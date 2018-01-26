import co.axelrod.vkphotodownloader.config.Config;
import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.UserAuthResponse;

import static com.sun.org.apache.xalan.internal.xsltc.compiler.Constants.REDIRECT_URI;

/**
 * Created by Vadim Axelrod (vadim@axelrod.co) on 27.01.2018.
 */
public class Main {
    public static void main(String[] args) {
        TransportClient transportClient = HttpTransportClient.getInstance();
        VkApiClient vk = new VkApiClient(transportClient);

        /*
        UserAuthResponse authResponse = vk.oauth()
                .userAuthorizationCodeFlow(Config, CLIENT_SECRET, REDIRECT_URI, code)
                .execute();


        UserActor actor = new UserActor(authResponse.getUserId(), authResponse.getAccessToken());
        */
    }
}
