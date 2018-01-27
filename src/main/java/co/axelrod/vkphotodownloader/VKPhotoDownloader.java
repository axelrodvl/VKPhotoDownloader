package co.axelrod.vkphotodownloader;

import co.axelrod.vkphotodownloader.config.TokenStorage;
import co.axelrod.vkphotodownloader.config.TokenStorageImpl;
import co.axelrod.vkphotodownloader.model.User;
import com.google.gson.*;
import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.UserAuthResponse;
import com.vk.api.sdk.queries.friends.FriendsGetOrder;
import com.vk.api.sdk.queries.users.UserField;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Vadim Axelrod (vadim@axelrod.co) on 27.01.2018.
 */
public class VKPhotoDownloader {
    private final static TokenStorage tokenStorage = new TokenStorageImpl();

    public static void main(String[] args) throws Exception {
        //String code = getCode(tokenStorage.getAppId());
        //https://oauth.vk.com/authorize?client_id=CLIENT_ID&display=page&redirect_uri=&scope=photos&response_type=code&v=5.71
        String code = "06c721d6ae6921343a";

        List<User> friends = new ArrayList<>();

        TransportClient transportClient = HttpTransportClient.getInstance();
        VkApiClient vk = new VkApiClient(transportClient);

        UserAuthResponse authResponse = vk.oauth()
                .userAuthorizationCodeFlow(Integer.valueOf(tokenStorage.getAppId()), tokenStorage.getClientSecret(), "", code)
                .execute();

        UserActor actor = new UserActor(authResponse.getUserId(), authResponse.getAccessToken());

        String response = vk.friends().get(actor, Arrays.asList(UserField.SCREEN_NAME, UserField.SEX, UserField.PHOTO_MAX_ORIG))
                .order(FriendsGetOrder.NAME)
                .executeAsString();

        JsonElement friendsJson = new JsonParser().parse(response).getAsJsonObject().get("response");
        Integer count = friendsJson.getAsJsonObject().get("count").getAsInt();
        System.out.println("Friends count:" + count);

        JsonArray friendsArray = friendsJson.getAsJsonObject().get("items").getAsJsonArray();
        for(JsonElement friend : friendsArray) {
            User user = new User(
                    friend.getAsJsonObject().get("id").getAsInt(),
                    friend.getAsJsonObject().get("first_name").getAsString(),
                    friend.getAsJsonObject().get("last_name").getAsString(),
                    friend.getAsJsonObject().get("screen_name").getAsString(),
                    friend.getAsJsonObject().get("sex").getAsInt(),
                    0,
                    new ArrayList<>());
            friends.add(user);
        }

        for(User user : friends) {
            try {
                parsePhotos(user, vk.photos().getAll(actor)
                        .count(10)
                        .ownerId(user.getId())
                        .skipHidden(false)
                        .photoSizes(true)
                        .executeAsString());
            } catch (Exception ex) {
                System.out.println("Unable to get photos list for user" + user);
            }
        }

        for(User user : friends) {
            System.out.println(user);
            try {
                int i = 0;
                for(String url : user.getPhotoUrls()) {
                    downloadPhoto(user, url, i++);
                }
            } catch (Exception ex) {
                System.out.println("Unable to download photo for user: " + user + "\n" + ex.getMessage());
            }
        }
    }

     private static void parsePhotos(User user, String response) throws Exception {
         JsonElement photosJson = new JsonParser().parse(response).getAsJsonObject().get("response");

         Integer count = photosJson.getAsJsonObject().get("count").getAsInt();
         System.out.println("Photos count:" + count);
         user.setPhotoCount(count);

         JsonArray photosArray = photosJson.getAsJsonObject().get("items").getAsJsonArray();

         for(JsonElement photo : photosArray) {
             JsonArray sizes = photo.getAsJsonObject().get("sizes").getAsJsonArray();
             // Последний - с максимальным размером ("type" : "z")
             user.getPhotoUrls().add(sizes.get(sizes.size() - 1).getAsJsonObject().get("src").getAsString());
         }
     }

    private static void downloadPhoto(User user, String url, Integer id) throws Exception {
        String username = user.getFirstName() + "_" + user.getLastName();
        String targetDirectory = "photo/" + username;

        File userDirectory = new File(targetDirectory);
        if (!userDirectory.exists()) {
            userDirectory.mkdirs();
        }

        URL website = new URL(url);
        ReadableByteChannel rbc = Channels.newChannel(website.openStream());
        String fileName = targetDirectory + "/" + username + "_" + id + ".jpg";
        System.out.println(url + " to " + fileName);
        FileOutputStream fos = new FileOutputStream(fileName);
        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
    }
}
