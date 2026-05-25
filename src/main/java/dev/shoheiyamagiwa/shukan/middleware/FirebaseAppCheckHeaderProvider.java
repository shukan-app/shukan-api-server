package dev.shoheiyamagiwa.shukan.middleware;

public interface FirebaseAppCheckHeaderProvider {
	boolean verifyAppCheckToken(String appCheckToken);
}
