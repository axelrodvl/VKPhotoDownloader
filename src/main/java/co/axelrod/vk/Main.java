package co.axelrod.vk;

import co.axelrod.vk.config.TokenStorage;
import co.axelrod.vk.config.TokenStorageImpl;

/**
 * Created by Vadim Axelrod (vadim@axelrod.co) on 31.01.2018.
 */
public class Main {
    public static void main(String[] args) throws Exception {
        String domain = args[0];
        String code = args[1];
        Integer photosToDownload = 50;
        TokenStorage tokenStorage = new TokenStorageImpl();

        VKPhotoDownloader vkPhotoDownloader = new VKPhotoDownloader(domain, tokenStorage);
        vkPhotoDownloader.downloadPhoto(code, photosToDownload);
    }
}
