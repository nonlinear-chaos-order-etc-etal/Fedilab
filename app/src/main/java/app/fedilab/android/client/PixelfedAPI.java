package app.fedilab.android.client;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import app.fedilab.android.R;
import app.fedilab.android.activities.MainActivity;
import app.fedilab.android.asynctasks.UpdateAccountInfoAsyncTask;
import app.fedilab.android.client.Entities.Account;
import app.fedilab.android.client.Entities.Error;
import app.fedilab.android.client.Entities.PixelFedStory;
import app.fedilab.android.client.Entities.PixelFedStoryItem;
import app.fedilab.android.helper.Helper;
import app.fedilab.android.sqlite.AccountDAO;
import app.fedilab.android.sqlite.Sqlite;

public class PixelfedAPI {



    private Context context;
    private PixelFedStory pixelFedStory;
    private int tootPerPage;
    private String instance;
    private String prefKeyOauthTokenT;
    private APIResponse apiResponse;
    private Error APIError;


    public PixelfedAPI(Context context) {
        this.context = context;
        if (context == null) {
            APIError = new Error();
            return;
        }
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        tootPerPage = sharedpreferences.getInt(Helper.SET_TOOT_PER_PAGE, Helper.TOOTS_PER_PAGE);
        if( MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.PIXELFED && tootPerPage > 30 ){
            tootPerPage = 30;
        }
        this.prefKeyOauthTokenT = sharedpreferences.getString(Helper.PREF_KEY_OAUTH_TOKEN, null);
        if (Helper.getLiveInstance(context) != null)
            this.instance = Helper.getLiveInstance(context);
        else {
            SQLiteDatabase db = Sqlite.getInstance(context, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
            String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
            String instance = sharedpreferences.getString(Helper.PREF_INSTANCE, Helper.getLiveInstance(context));
            Account account = new AccountDAO(context, db).getUniqAccount(userId, instance);
            if (account == null) {
                APIError = new Error();
                APIError.setError(context.getString(R.string.toast_error));
                return;
            }
            this.instance = account.getInstance().trim();
        }
        apiResponse = new APIResponse();
        APIError = null;
    }


    /**
     * Retrieves Pixelfed Own Stories *synchronously*
     *
     * @return APIResponse
     */
    public APIResponse getMyStories() {

        try {
            HttpsConnection httpsConnection = new HttpsConnection(context, this.instance);
            String response = httpsConnection.get(getAbsoluteUrl("/stories/v1/me"), 10, null, prefKeyOauthTokenT);
            apiResponse.setSince_id(httpsConnection.getSince_id());
            apiResponse.setMax_id(httpsConnection.getMax_id());
            pixelFedStory = parseStory(new JSONObject(response));
        } catch (HttpsConnection.HttpsConnectionException e) {
            setError(e.getStatusCode(), e);
        } catch (NoSuchAlgorithmException | IOException | KeyManagementException | JSONException e) {
            e.printStackTrace();
        }
        if (apiResponse == null)
            apiResponse = new APIResponse();
        apiResponse.setPixelFedStory(pixelFedStory);
        return apiResponse;
    }



    /**
     * Parse a single item for stories
     * @param jsonObject JSONObject
     * @return PixelFedStoryItem
     */
    private static PixelFedStory parseStory(JSONObject jsonObject) {
        PixelFedStory pixelFedStory = new PixelFedStory();
        try {
            pixelFedStory.setId(jsonObject.getString("id"));
            pixelFedStory.setPhoto(jsonObject.getString("photo"));
            pixelFedStory.setName(jsonObject.getString("name"));
            pixelFedStory.setLink(jsonObject.getString("link"));
            pixelFedStory.setLastUpdated(new Date(Long.valueOf(jsonObject.getString("lastUpdated"))));
            pixelFedStory.setSeen(jsonObject.getBoolean("seen"));
            pixelFedStory.setPixelFedStoryItems(parseStoryItems(jsonObject.getJSONArray("items")));
        } catch (JSONException ignored) {
        }
        return pixelFedStory;
    }



    /**
     * Parse json response for several items for stories
     *
     * @param jsonArray JSONArray
     * @return List<PixelFedStoryItem>
     */
    public static List<PixelFedStoryItem> parseStoryItems(JSONArray jsonArray) {

        List<PixelFedStoryItem> pixelFedStoryItems = new ArrayList<>();
        try {
            int i = 0;
            while (i < jsonArray.length()) {

                JSONObject resobj = jsonArray.getJSONObject(i);
                PixelFedStoryItem pixelFedStoryItem = parseStoryItem(resobj);
                i++;
                pixelFedStoryItems.add(pixelFedStoryItem);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return pixelFedStoryItems;
    }


    /**
     * Parse a single item for stories
     * @param jsonObject JSONObject
     * @return PixelFedStoryItem
     */
    private static PixelFedStoryItem parseStoryItem(JSONObject jsonObject) {
        PixelFedStoryItem pixelFedStoryItem = new PixelFedStoryItem();
        try {
            pixelFedStoryItem.setId(jsonObject.getString("id"));
            pixelFedStoryItem.setType(jsonObject.getString("type"));
            pixelFedStoryItem.setLength(jsonObject.getInt("length"));
            pixelFedStoryItem.setSrc(jsonObject.getString("src"));
            pixelFedStoryItem.setPreview(jsonObject.getString("preview"));
            pixelFedStoryItem.setLink(jsonObject.getString("link"));
            pixelFedStoryItem.setLinkText(jsonObject.getString("linkText"));
            pixelFedStoryItem.setTime(new Date(Long.valueOf(jsonObject.getString("time"))));
            pixelFedStoryItem.setExpires_at(new Date(Long.valueOf(jsonObject.getString("expires_at"))));
            pixelFedStoryItem.setSeen(jsonObject.getBoolean("seen"));
        } catch (JSONException ignored) {
        }
        return pixelFedStoryItem;
    }


    /**
     * Set the error message
     *
     * @param statusCode int code
     * @param error      Throwable error
     */
    private void setError(int statusCode, Throwable error) {
        APIError = new Error();
        APIError.setStatusCode(statusCode);
        String message = statusCode + " - " + error.getMessage();
        try {
            JSONObject jsonObject = new JSONObject(Objects.requireNonNull(error.getMessage()));
            String errorM = jsonObject.get("error").toString();
            message = "Error " + statusCode + " : " + errorM;
        } catch (JSONException e) {
            if (error.getMessage().split(".").length > 0) {
                String errorM = error.getMessage().split(".")[0];
                message = "Error " + statusCode + " : " + errorM;
            }
        }
        APIError.setError(message);
        apiResponse.setError(APIError);
    }

    private void setDefaultError(Exception e) {
        APIError = new Error();
        if( apiResponse == null){
            apiResponse = new APIResponse();
        }
        if (e.getLocalizedMessage() != null && e.getLocalizedMessage().trim().length() > 0)
            APIError.setError(e.getLocalizedMessage());
        else if (e.getMessage() != null && e.getMessage().trim().length() > 0)
            APIError.setError(e.getMessage());
        else
            APIError.setError(context.getString(R.string.toast_error));
        apiResponse.setError(APIError);
    }


    public Error getError() {
        return APIError;
    }



    private String getAbsoluteUrl(String action) {
        return Helper.instanceWithProtocol(this.context, this.instance) + "/api" + action;
    }
}
