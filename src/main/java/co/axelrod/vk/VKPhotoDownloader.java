package co.axelrod.vk;

import co.axelrod.vk.config.TokenStorage;
import co.axelrod.vk.config.TokenStorageImpl;
import co.axelrod.vk.model.Photo;
import co.axelrod.vk.model.User;
import com.google.gson.*;
import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.UserAuthResponse;
import com.vk.api.sdk.queries.friends.FriendsGetOrder;
import com.vk.api.sdk.queries.users.UserField;

import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.*;

import static co.axelrod.vk.util.FileUtils.delete;

/**
 * Created by Vadim Axelrod (vadim@axelrod.co) on 27.01.2018.
 */
public class VKPhotoDownloader {
    private final static TokenStorage tokenStorage = new TokenStorageImpl();

    public static void main(String[] args) throws Exception {
        //String code = getCode(tokenStorage.getAppId());
        //https://oauth.vk.com/authorize?client_id=CLIENT_ID&display=page&redirect_uri=&scope=photos&response_type=code&v=5.71
        System.out.println("Starting VKPhotoDownloader");

        Integer photosToDownload = 50;

        String code = args[0];
        //String code = "1e7d541aaf703cde66";

        List<User> friends = new ArrayList<>();
        File photoDirectory = new File("photo");
        if (photoDirectory.exists()) {
            delete(photoDirectory);
        }

        TransportClient transportClient = HttpTransportClient.getInstance();
        VkApiClient vk = new VkApiClient(transportClient);

        UserAuthResponse authResponse = vk.oauth()
                .userAuthorizationCodeFlow(Integer.valueOf(tokenStorage.getAppId()), tokenStorage.getClientSecret(), "http://localhost:4567/auth", code)
                .execute();

        UserActor actor = new UserActor(authResponse.getUserId(), authResponse.getAccessToken());

        String response = vk.friends().get(actor, Arrays.asList(UserField.SCREEN_NAME, UserField.SEX, UserField.PHOTO_MAX_ORIG))
                .order(FriendsGetOrder.NAME)
                .executeAsString();

        JsonElement friendsJson = new JsonParser().parse(response).getAsJsonObject().get("response");
        Integer count = friendsJson.getAsJsonObject().get("count").getAsInt();
        System.out.println("Friends total count:" + count);

        JsonArray friendsArray = friendsJson.getAsJsonObject().get("items").getAsJsonArray();
        for(JsonElement friend : friendsArray) {
            if(friend.getAsJsonObject().get("sex").getAsInt() == 1) {
                User user = new User(
                        friend.getAsJsonObject().get("id").getAsInt(),
                        friend.getAsJsonObject().get("first_name").getAsString(),
                        friend.getAsJsonObject().get("last_name").getAsString(),
                        friend.getAsJsonObject().get("sex").getAsInt(),
                        0,
                        new ArrayList<>());
                friends.add(user);
            }
        }
        System.out.println("Friends (FEMALE) total count:" + friends.size());

        for(User user : friends) {
            try {
                response = vk.photos().getAll(actor)
                        .count(photosToDownload)
                        .ownerId(user.getId())
                        .skipHidden(false)
                        .photoSizes(true)
                        .unsafeParam("extended", 1)
                        .executeAsString();
                parsePhotos(user, response);
                savePhotosJSON(user, response);
            } catch (Exception ex) {
                System.out.println("Unable to get photos list for user" + user);
            }
        }

        for(User user : friends) {
            System.out.println(user);
            try {
                for(Photo photo : user.getPhotos()) {
                    downloadPhoto(user, photo);
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

         JsonArray photosArrayJson = photosJson.getAsJsonObject().get("items").getAsJsonArray();

         List<JsonElement> photosArray = new ArrayList<>();
         for(JsonElement photo : photosArrayJson) {
             photosArray.add(photo);
         }

         Collections.sort(photosArray, new Comparator<JsonElement>() {
             public int compare(JsonElement photo1, JsonElement photo2) {
                 Integer likes1 = photo1.getAsJsonObject().get("likes").getAsJsonObject().get("count").getAsInt();
                 Integer likes2 = photo2.getAsJsonObject().get("likes").getAsJsonObject().get("count").getAsInt();

                 return (int) Float.compare(likes2, likes1);
             }
         });

         for(JsonElement photo : photosArray) {
             Photo photoObject = new Photo();
             photoObject.setId(photo.getAsJsonObject().get("id").getAsString());
             photoObject.setLikes(photo.getAsJsonObject().get("likes").getAsJsonObject().get("count").getAsInt());

             if(photoObject.getLikes() != 0) {
                 JsonArray sizes = photo.getAsJsonObject().get("sizes").getAsJsonArray();

                 for (JsonElement size : sizes) {
                     if (size.getAsJsonObject().get("type").getAsString().equals("r")) {
                         photoObject.setUrl(size.getAsJsonObject().get("src").getAsString());
                         user.getPhotos().add(photoObject);
                         break;
                     }
                 }
             }
         }
     }

    private static void downloadPhoto(User user, Photo photo) throws Exception {
        String targetDirectory = "photo/" + user.getId();

        File userDirectory = new File(targetDirectory);
        if (!userDirectory.exists()) {
            userDirectory.mkdirs();
        }

        String filePath = targetDirectory + "/" + photo.getId() + "_" + photo.getLikes() + ".jpg";

        // Check if photo already exists
        File photoFile = new File(filePath);
        if(!photoFile.exists()) {
            System.out.println("Downloading " + photo.getUrl() + " to " + filePath);

            URL website = new URL(photo.getUrl());
            ReadableByteChannel rbc = Channels.newChannel(website.openStream());
            FileOutputStream fos = new FileOutputStream(filePath);
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        } else {
            System.out.println("Photo " + photo.getUrl() + " already exists in " + filePath);
        }
    }

    private static void savePhotosJSON(User user, String response) throws Exception {
        String targetDirectory = "users";

        File userDirectory = new File(targetDirectory);
        if (!userDirectory.exists()) {
            userDirectory.mkdirs();
        }

        try (PrintStream out = new PrintStream(new FileOutputStream("users/" + user.getId() + ".json"))) {
            out.println(response);
        }

        InputStream is = new FileInputStream("users/" + user.getId() + ".json");
        BufferedReader buf = new BufferedReader(new InputStreamReader(is));
    }
}
