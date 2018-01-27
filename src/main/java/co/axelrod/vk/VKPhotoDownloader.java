package co.axelrod.vk;

import co.axelrod.vk.config.TokenStorage;
import co.axelrod.vk.config.TokenStorageImpl;
import co.axelrod.vk.model.User;
import co.axelrod.vk.util.StringUtils;
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

        Integer photosToDownload = 200;

        String code = args[0];
        //String code = "1ae3e25a5d81220de4";

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
                        new HashMap<>());
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
                for(Map.Entry<String, String> photo : user.getPhotoUrls().entrySet()) {
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

         JsonArray photosArray = photosJson.getAsJsonObject().get("items").getAsJsonArray();

         for(JsonElement photo : photosArray) {
             JsonArray sizes = photo.getAsJsonObject().get("sizes").getAsJsonArray();

             for(JsonElement size : sizes) {
                 if(size.getAsJsonObject().get("type").getAsString().equals("r")) {
                     user.getPhotoUrls().put(photo.getAsJsonObject().get("id").getAsString(),
                             size.getAsJsonObject().get("src").getAsString());
                     break;
                 }
             }
         }
     }

    private static void downloadPhoto(User user, Map.Entry<String, String> photo) throws Exception {
        String username = StringUtils.transliterate(user.getFirstName() + "_" + user.getLastName());
        String targetDirectory = "photo/" + user.getId();

        File userDirectory = new File(targetDirectory);
        if (!userDirectory.exists()) {
            userDirectory.mkdirs();
        }

        String filePath = targetDirectory + "/" + photo.getKey() + ".jpg";

        // Check if photo already exists
        File photoFile = new File(filePath);
        if(!photoFile.exists()) {
            System.out.println("Downloading " + photo.getValue() + " to " + filePath);

            URL website = new URL(photo.getValue());
            ReadableByteChannel rbc = Channels.newChannel(website.openStream());
            FileOutputStream fos = new FileOutputStream(filePath);
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        } else {
            System.out.println("Photo " + photo.getValue() + " already exists in " + filePath);
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
