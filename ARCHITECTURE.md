# Architecture Rules

## Principle

- `Entity` , `Service`, `Repository`, `Controller` から構成される4層アーキテクチャを採用し、単方向の依存関係を遵守する
- 依存方向は `Controller` → `Service` → `Repository` → `DAO`
- すべてのクラスに `final` 修飾子を付与し、継承を禁止する (必要な場合はその都度指示を仰いで下さい)

## Entity

### 対象

- `Entity` クラスが該当する
- `Entity` は、ドメインデータおよびドメインロジックを保持する純粋なオブジェクトとする

### 配置

- `dev.shoheiyamagiwa.shukan.entity` 配下に配置する

## Service

### 対象

- `Service` クラス,  `Repository` インターフェース, `RepositoryProvider` インターフェース および `TransactionManager`
  インターフェース が該当する
- `Service` クラスは、ドメインロジックを実装するクラス
- `Repository` インターフェースは、データアクセスの抽象化を提供するインターフェース
- `RepositoryProvider` インターフェースは、複数の `Repository` インターフェースをまとめて提供するインターフェース
- `TransactionManager` インターフェースは、トランザクションの管理を提供するインターフェース

### 実装例

`Unit of Work` パターンを採用する

```java
public interface UserRepository {
	void save(User user);
}

public interface ProfileRepository {
	void save(Profile profile);
}

public interface RepositoryProvider {
	UserRepository getUserRepository();

	ProfileRepository getProfileRepository();
}

public interface TransactionManager {
	<T> T executeInTransaction(Function<RepositoryProvider, T> operation);
}

public final class UserService {
	private final TransactionManager transactionManager;

	public UserService(TransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	public void registerUser(User user, Profile profile) {
		transactionManager.executeInTransaction(provider -> {
			provider.getUserRepository().save(user);
			provider.getProfileRepository().save(profile);
			return null;
		});
	}
}
```

### 配置

- `dev.shoheiyamagiwa.shukan.service` 配下に配置する

### ファイル名規約

- `Service` クラスのクラス名には必ず `Service` を最後につける (`UserService` など)
- `Repository` インターフェースの名前には必ず `Repository` を最後につける (`UserRepository` など)
- `RepositoryProvider` インターフェースの名前には必ず `RepositoryProvider` を最後につける (`UserRepositoryProvider`
  など)
- `TransactionManager` インターフェースの名前には必ず `TransactionManager` を最後につける (
  `UserServiceTransactionManager` など)

## Repository

### 対象

- `Repository` クラス, 各種 `DAO` クラス (`Data Access Object`) および 各種 `DTO` クラスが該当する
- `Repository` クラスは `Service` 層の `Repository` インターフェースを実装し、各種 `DAO` のオーケストレーションを担当するクラス
- `DAO` クラスは具体的なデータ源への手続きを提供するクラス (`PostgresUserDao`, `RedisUserDao` など)
- `DTO` クラスは、DBのレコードを表すレコードクラス

### 実装例

- 基本的に `TransactionManager` で `Postgres` のトランザクション処理を行う (`Redis` の処理はトランザクションなし)
- そのため `Redis` に関してはトランザクション処理なしで整合性を保てるように実装する
  (基本的にRedisの処理が失敗した場合は再試行はせず、データ読み取り時にキャッシュを再構築する)

```java
import java.sql.Connection;

public final class PostgresUserDao {
	private final Connection connection;

	public PostgresUserDao(Connection connection) {
		this.connection = connection;
	}

	public Optional<PostgresUserDto> selectById(String id) {
		/* SELECT */
	}

	public void upsert(String id, String name) {
		/* INSERT/UPDATE */
	}
}

public final class RedisUserDao {
	private final JedisPool jedisPool;

	public RedisUserDao(JedisPool jedisPool) {
		this.jedisPool = jedisPool;
	}

	public Optional<RedisUserDto> get(String id) {
		/* GET & Deserialize */
	}

	public void set(String id, String name) {
		/* SETEX */
	}

	public void delete(String id) {
		/* DEL */
	}
}

// ProfileRepositoryImplも同様に実装する
public final class UserRepositoryImpl implements UserRepository {
	private final PostgresUserDao postgresDao;
	private final RedisUserDao redisDao;

	public UserRepositoryImpl(PostgresUserDao postgresDao, RedisUserDao redisDao) {
		this.postgresDao = postgresDao;
		this.redisDao = redisDao;
	}

	@Override
	public Optional<User> findById(String id) {
		// Check for cached data
		Optional<RedisUserDto> redisDtoOpt = redisDao.get(id);
		if (redisDtoOpt.isPresent()) {
			return Optional.of(toDomainEntity(redisDtoOpt.get()));
		}

		// Check for data in a Database
		Optional<PostgresUserDto> pgDtoOpt = postgresDao.selectById(id);

		if (pgDtoOpt.isPresent()) {
			User user = toDomainEntity(pgDtoOpt.get());

			redisDao.set(user.getId(), user.getName());

			return Optional.of(user);
		}

		return Optional.empty();
	}

	@Override
	public void save(User user) {
		PostgresUserDto pgDto = toPostgresDto(user);
		postgresDao.upsert(pgDto.id(), pgDto.name());

		redisDao.delete(user.getId());
	}

	// Mapping Method
	private User toDomainEntity(PostgresUserDto dto) {
		return new User(dto.id(), dto.name());
	}

	private User toDomainEntity(RedisUserDto dto) {
		return new User(dto.id(), dto.name());
	}

	private PostgresUserDto toPostgresDto(User entity) {
		return new PostgresUserDto(entity.getId(), entity.getName());
	}

	private RedisUserDto toRedisDto(User entity) {
		return new RedisUserDto(entity.getId(), entity.getName());
	}
}

public final class JdbcRepositoryProvider implements RepositoryProvider {
	private final Connection connection;
	private final JedisPool jedisPool;

	private UserRepository userRepository;
	private ProfileRepository profileRepository;

	public JdbcRepositoryProvider(Connection connection, JedisPool jedisPool) {
		this.connection = connection;
		this.jedisPool = jedisPool;
	}

	@Override
	public UserRepository getUserRepository() {
		if (this.userRepository == null) {
			PostgresUserDao postgresDao = new PostgresUserDao(this.connection);
			RedisUserDao redisDao = new RedisUserDao(this.jedisPool);

			this.userRepository = new UserRepositoryImpl(postgresDao, redisDao);
		}
		return this.userRepository;
	}

	@Override
	public ProfileRepository getProfileRepository() {
		if (this.profileRepository == null) {
			PostgresProfileDao postgresDao = new PostgresProfileDao(this.connection);
			RedisProfileDao redisDao = new RedisProfileDao(this.jedisPool);

			this.profileRepository = new ProfileRepositoryImpl(postgresDao, redisDao);
		}
		return this.profileRepository;
	}
}

public final class JdbcTransactionManager implements TransactionManager {
	private final DataSource dataSource;
	private final JedisPool jedisPool;

	public JdbcTransactionManager(DataSource dataSource, JedisPool jedisPool) {
		this.dataSource = dataSource;
		this.jedisPool = jedisPool;
	}

	@Override
	public <T> T executeInTransaction(Function<RepositoryProvider, T> operation) {
		try (Connection conn = dataSource.getConnection()) {
			conn.setAutoCommit(false);

			try {
				RepositoryProvider provider = new JdbcRepositoryProvider(conn, jedisPool);

				T result = operation.apply(provider);

				conn.commit();
				return result;

			} catch (Exception e) {
				conn.rollback();
				throw new RuntimeException("Transaction failed and rolled back", e);
			}
		} catch (SQLException e) {
			throw new InfrastructureException("Database connection error", e);
		}
	}
}
```

### 配置

- `dev.shoheiyamagiwa.shukan.repository` 配下に配置する

### ファイル名規約

- `Repository` クラスの名前には必ず `Impl` を最後につける (`UserRepositoryImpl` など)
- `DAO` クラスの名前には必ず `Dao` を最後につける (`PostgresUserDao` など)
- `DTO` クラスの名前には必ず `Dto` を最後につける (`UserDto` など)

## Controller

- `Main` クラス, `Controller` クラス, `RequestDto` クラスおよび `ResponseDto` クラス が該当する
- `Main` クラスは `Javalin` を用いてアプリケーションを起動し、リクエストのルーティングを行い `Controller` に処理を委譲する
- `Controller` クラスは、リクエストを受けて、`Service` 層に処理を委譲し、レスポンスを返すクラス
- `Main` クラスは `dev.shoheiyamagiwa.shukan` 配下に配置する
- `Controller` クラスは `dev.shoheiyamagiwa.shukan.controller` 配下に配置する
- `Controller` クラスの名前には必ず `Controller` を最後につける (`UserController` など)
