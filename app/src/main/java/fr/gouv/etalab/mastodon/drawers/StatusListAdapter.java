package fr.gouv.etalab.mastodon.drawers;
/* Copyright 2017 Thomas Schneider
 *
 * This file is a part of Mastalab
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 3 of the
 * License, or (at your option) any later version.
 *
 * Mastalab is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Thomas Schneider; if not,
 * see <http://www.gnu.org/licenses>. */

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import fr.gouv.etalab.mastodon.activities.MediaActivity;
import fr.gouv.etalab.mastodon.activities.ShowConversationActivity;
import fr.gouv.etalab.mastodon.activities.TootActivity;
import fr.gouv.etalab.mastodon.client.Entities.Error;
import fr.gouv.etalab.mastodon.client.PatchBaseImageDownloader;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.interfaces.OnTranslatedInterface;
import fr.gouv.etalab.mastodon.translation.YandexQuery;
import mastodon.etalab.gouv.fr.mastodon.R;
import fr.gouv.etalab.mastodon.activities.ShowAccountActivity;
import fr.gouv.etalab.mastodon.asynctasks.PostActionAsyncTask;
import fr.gouv.etalab.mastodon.asynctasks.RetrieveFeedsAsyncTask;
import fr.gouv.etalab.mastodon.client.API;
import fr.gouv.etalab.mastodon.client.Entities.Attachment;
import fr.gouv.etalab.mastodon.client.Entities.Status;
import fr.gouv.etalab.mastodon.interfaces.OnPostActionInterface;

import static fr.gouv.etalab.mastodon.activities.MainActivity.currentLocale;
import static fr.gouv.etalab.mastodon.helper.Helper.changeDrawableColor;


/**
 * Created by Thomas on 24/04/2017.
 * Adapter for Status
 */
public class StatusListAdapter extends BaseAdapter implements OnPostActionInterface, OnTranslatedInterface {

    private Context context;
    private List<Status> statuses;
    private LayoutInflater layoutInflater;
    private ImageLoader imageLoader;
    private DisplayImageOptions options;
    private boolean isOnWifi;
    private int behaviorWithAttachments;
    private StatusListAdapter statusListAdapter;
    private final int REBLOG = 1;
    private final int FAVOURITE = 2;
    private ViewHolder holder;
    private RetrieveFeedsAsyncTask.Type type;
    private String targetedId;

    public StatusListAdapter(Context context, RetrieveFeedsAsyncTask.Type type, String targetedId, boolean isOnWifi, int behaviorWithAttachments, List<Status> statuses){
        this.context = context;
        this.statuses = statuses;
        this.isOnWifi = isOnWifi;
        this.behaviorWithAttachments = behaviorWithAttachments;
        layoutInflater = LayoutInflater.from(this.context);
        statusListAdapter = this;
        this.type = type;
        this.targetedId = targetedId;
    }



    @Override
    public int getCount() {
        return statuses.size();
    }

    @Override
    public Object getItem(int position) {
        return statuses.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {


        imageLoader = ImageLoader.getInstance();
        File cacheDir = new File(context.getCacheDir(), context.getString(R.string.app_name));
        ImageLoaderConfiguration configImg = new ImageLoaderConfiguration.Builder(context)
                .imageDownloader(new PatchBaseImageDownloader(context))
                .threadPoolSize(5)
                .threadPriority(Thread.MIN_PRIORITY + 3)
                .denyCacheImageMultipleSizesInMemory()
                .diskCache(new UnlimitedDiskCache(cacheDir))
                .build();
        imageLoader.init(configImg);
        options = new DisplayImageOptions.Builder().displayer(new SimpleBitmapDisplayer()).cacheInMemory(false)
                .cacheOnDisk(true).resetViewBeforeLoading(true).build();

        final Status status = statuses.get(position);
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.drawer_status, parent, false);
            holder = new ViewHolder();
            holder.status_document_container = (LinearLayout) convertView.findViewById(R.id.status_document_container);
            holder.status_content = (TextView) convertView.findViewById(R.id.status_content);
            holder.status_content_translated = (TextView) convertView.findViewById(R.id.status_content_translated);
            holder.status_account_username = (TextView) convertView.findViewById(R.id.status_account_username);
            holder.status_account_displayname = (TextView) convertView.findViewById(R.id.status_account_displayname);
            holder.status_account_profile = (ImageView) convertView.findViewById(R.id.status_account_profile);
            holder.status_account_profile_boost = (ImageView) convertView.findViewById(R.id.status_account_profile_boost);
            holder.status_account_profile_boost_by = (ImageView) convertView.findViewById(R.id.status_account_profile_boost_by);
            holder.status_favorite_count = (TextView) convertView.findViewById(R.id.status_favorite_count);
            holder.status_reblog_count = (TextView) convertView.findViewById(R.id.status_reblog_count);
            holder.status_toot_date = (TextView) convertView.findViewById(R.id.status_toot_date);
            holder.status_show_more = (Button) convertView.findViewById(R.id.status_show_more);
            holder.status_more = (ImageView) convertView.findViewById(R.id.status_more);
            holder.status_reblog_user = (TextView) convertView.findViewById(R.id.status_reblog_user);
            holder.status_action_container = (LinearLayout) convertView.findViewById(R.id.status_action_container);
            holder.status_prev1 = (ImageView) convertView.findViewById(R.id.status_prev1);
            holder.status_prev2 = (ImageView) convertView.findViewById(R.id.status_prev2);
            holder.status_prev3 = (ImageView) convertView.findViewById(R.id.status_prev3);
            holder.status_prev4 = (ImageView) convertView.findViewById(R.id.status_prev4);
            holder.status_prev1_play = (ImageView) convertView.findViewById(R.id.status_prev1_play);
            holder.status_prev2_play = (ImageView) convertView.findViewById(R.id.status_prev2_play);
            holder.status_prev3_play = (ImageView) convertView.findViewById(R.id.status_prev3_play);
            holder.status_prev4_play = (ImageView) convertView.findViewById(R.id.status_prev4_play);
            holder.status_container2 = (LinearLayout) convertView.findViewById(R.id.status_container2);
            holder.status_container3 = (LinearLayout) convertView.findViewById(R.id.status_container3);
            holder.status_prev4_container = (RelativeLayout) convertView.findViewById(R.id.status_prev4_container);
            holder.status_reply = (ImageView) convertView.findViewById(R.id.status_reply);
            holder.status_privacy = (ImageView) convertView.findViewById(R.id.status_privacy);
            holder.status_translate = (Button) convertView.findViewById(R.id.status_translate);
            holder.status_content_translated_container = (LinearLayout) convertView.findViewById(R.id.status_content_translated_container);
            holder.main_container = (LinearLayout) convertView.findViewById(R.id.main_container);
            holder.status_spoiler_container = (LinearLayout) convertView.findViewById(R.id.status_spoiler_container);
            holder.status_content_container = (LinearLayout) convertView.findViewById(R.id.status_content_container);
            holder.status_spoiler = (TextView) convertView.findViewById(R.id.status_spoiler);
            holder.status_spoiler_button = (Button) convertView.findViewById(R.id.status_spoiler_button);
            holder.yandex_translate = (TextView) convertView.findViewById(R.id.yandex_translate);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if( status.getSpoiler_text() != null && status.getSpoiler_text().trim().length() > 0 && !status.isSpoilerShown()){
            holder.status_content_container.setVisibility(View.GONE);
            holder.status_spoiler_container.setVisibility(View.VISIBLE);
            holder.status_spoiler_button.setVisibility(View.VISIBLE);
            holder.status_spoiler.setVisibility(View.VISIBLE);
        }else {
            holder.status_spoiler_button.setVisibility(View.GONE);
            holder.status_content_container.setVisibility(View.VISIBLE);
            if( status.getSpoiler_text() != null && status.getSpoiler_text().trim().length() > 0 )
                holder.status_spoiler_container.setVisibility(View.VISIBLE);
            else
                holder.status_spoiler_container.setVisibility(View.GONE);
        }
        if( status.getSpoiler_text() != null)
            holder.status_spoiler.setText(status.getSpoiler_text());
        //Spoiler opens
        holder.status_spoiler_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                status.setSpoilerShown(true);
                holder.status_spoiler_button.setVisibility(View.GONE);
                statusListAdapter.notifyDataSetChanged();
            }
        });
        if( currentLocale != null && status.getLanguage() != null && !status.getLanguage().trim().equals(currentLocale) && !status.getLanguage().trim().equals("null")){
            holder.status_translate.setVisibility(View.VISIBLE);
        }else {
            holder.status_translate.setVisibility(View.GONE);
        }

        holder.status_translate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if( !status.isTranslated() ){
                        new YandexQuery(StatusListAdapter.this).getYandexTextview(position, status.getContent(), currentLocale);
                    }else {
                        status.setTranslationShown(!status.isTranslationShown());
                        statusListAdapter.notifyDataSetChanged();
                    }
                } catch (JSONException e) {
                   Toast.makeText(context, R.string.toast_error_translate, Toast.LENGTH_LONG).show();
                }
            }
        });

        holder.yandex_translate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://translate.yandex.com/"));
                context.startActivity(browserIntent);
            }
        });
        //Toot was translated and user asked to see it
        if( status.isTranslationShown()){
            holder.status_content.setVisibility(View.GONE);
            holder.status_content_translated_container.setVisibility(View.VISIBLE);
        }else { //Toot is not translated
            holder.status_content.setVisibility(View.VISIBLE);
            holder.status_content_translated_container.setVisibility(View.GONE);
        }

        //Hides action bottom bar action when looking to status trough accounts
        if( type == RetrieveFeedsAsyncTask.Type.USER){
            holder.status_action_container.setVisibility(View.GONE);
        }
        //Manages theme for icon colors
        final SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        if( theme == Helper.THEME_DARK){
            changeDrawableColor(context, R.drawable.ic_reply,R.color.dark_text);
            changeDrawableColor(context, R.drawable.ic_action_more,R.color.dark_text);
            changeDrawableColor(context, R.drawable.ic_action_globe,R.color.dark_text);
            changeDrawableColor(context, R.drawable.ic_action_lock_open,R.color.dark_text);
            changeDrawableColor(context, R.drawable.ic_action_lock_closed,R.color.dark_text);
            changeDrawableColor(context, R.drawable.ic_local_post_office,R.color.dark_text);
            changeDrawableColor(context, R.drawable.ic_retweet_black,R.color.dark_text);
            changeDrawableColor(context, R.drawable.ic_fav_black,R.color.dark_text);
            changeDrawableColor(context, R.drawable.ic_photo,R.color.dark_text);
            changeDrawableColor(context, R.drawable.ic_remove_red_eye,R.color.dark_text);
            changeDrawableColor(context, R.drawable.ic_translate,R.color.dark_text);
        }else {
            changeDrawableColor(context, R.drawable.ic_reply,R.color.black);
            changeDrawableColor(context, R.drawable.ic_action_more,R.color.black);
            changeDrawableColor(context, R.drawable.ic_action_globe,R.color.black);
            changeDrawableColor(context, R.drawable.ic_action_lock_open,R.color.black);
            changeDrawableColor(context, R.drawable.ic_action_lock_closed,R.color.black);
            changeDrawableColor(context, R.drawable.ic_local_post_office,R.color.black);
            changeDrawableColor(context, R.drawable.ic_retweet_black,R.color.black);
            changeDrawableColor(context, R.drawable.ic_fav_black,R.color.black);
            changeDrawableColor(context, R.drawable.ic_photo,R.color.black);
            changeDrawableColor(context, R.drawable.ic_remove_red_eye,R.color.black);
            changeDrawableColor(context, R.drawable.ic_translate,R.color.black);
        }

        //Redraws top icons (boost/reply)
        final float scale = context.getResources().getDisplayMetrics().density;
        if( !status.getIn_reply_to_account_id().equals("null") || !status.getIn_reply_to_id().equals("null") ){
            Drawable img = ContextCompat.getDrawable(context, R.drawable.ic_reply);
            img.setBounds(0,0,(int) (20 * scale + 0.5f),(int) (15 * scale + 0.5f));
            holder.status_account_displayname.setCompoundDrawables( img, null, null, null);
        }else if( status.getReblog() != null){
            Drawable img = ContextCompat.getDrawable(context, R.drawable.ic_retweet_black);
            img.setBounds(0,0,(int) (20 * scale + 0.5f),(int) (15 * scale + 0.5f));
            holder.status_account_displayname.setCompoundDrawables( img, null, null, null);
        }else{
            holder.status_account_displayname.setCompoundDrawables( null, null, null, null);
        }

        //Click on a conversation
        if( type != RetrieveFeedsAsyncTask.Type.CONTEXT ){
            holder.status_content.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, ShowConversationActivity.class);
                    Bundle b = new Bundle();
                    b.putString("statusId", status.getId());
                    intent.putExtras(b);
                    context.startActivity(intent);
                }
            });
        }else {
            if( theme == Helper.THEME_LIGHT){
                if( position == ShowConversationActivity.position){
                    holder.main_container.setBackgroundResource(R.color.blue_light);
                }else {
                    holder.main_container.setBackgroundResource(R.color.white);
                }
            }else {
                if( position == ShowConversationActivity.position){
                    holder.main_container.setBackgroundResource(R.color.header2D);
                }else {
                    holder.main_container.setBackgroundResource(R.color.cardview_dark_background);
                }
            }
        }

        final String content, displayName, username, ppurl;
        if( status.getReblog() != null){
            content = status.getReblog().getContent();
            displayName = Helper.shortnameToUnicode(status.getReblog().getAccount().getDisplay_name(), true);
            username = status.getReblog().getAccount().getUsername();
            holder.status_reblog_user.setText(displayName + " " +String.format("@%s",username));
            ppurl = status.getReblog().getAccount().getAvatar();
            holder.status_reblog_user.setVisibility(View.VISIBLE);
            holder.status_account_displayname.setText(context.getResources().getString(R.string.reblog_by, status.getAccount().getUsername()));
            holder.status_account_username.setText( "");
        }else {
            ppurl = status.getAccount().getAvatar();
            content = status.getContent();
            displayName = Helper.shortnameToUnicode(status.getAccount().getDisplay_name(), true);
            username = status.getAccount().getUsername();
            holder.status_reblog_user.setVisibility(View.GONE);
            holder.status_account_displayname.setText(displayName);
            holder.status_account_username.setText(String.format("@%s",username));
        }

        holder.status_reply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, TootActivity.class);
                Bundle b = new Bundle();
                b.putParcelable("tootReply", status);
                intent.putExtras(b); //Put your id to your next Intent
                context.startActivity(intent);
                if( type == RetrieveFeedsAsyncTask.Type.CONTEXT ){
                    try {
                        //Avoid to open multi activities when replying in a conversation
                        ((ShowConversationActivity)context).finish();
                    }catch (Exception ignored){}

                }
            }
        });


        holder.status_content = Helper.clickableElements(context, holder.status_content,content,
                status.getReblog() != null?status.getReblog().getMentions():status.getMentions());

        if( status.getContent_translated() != null && status.getContent_translated().length() > 0){
            holder.status_content_translated = Helper.clickableElements(context, holder.status_content_translated,status.getContent_translated(),
                    status.getReblog() != null?status.getReblog().getMentions():status.getMentions());
        }
        holder.status_favorite_count.setText(String.valueOf(status.getFavourites_count()));
        holder.status_reblog_count.setText(String.valueOf(status.getReblogs_count()));
        holder.status_toot_date.setText(Helper.dateDiff(context, status.getCreated_at()));
        

        if( status.getReblog() != null) {
            imageLoader.displayImage(ppurl, holder.status_account_profile_boost, options);
            imageLoader.displayImage(status.getAccount().getAvatar(), holder.status_account_profile_boost_by, options);
            holder.status_account_profile_boost.setVisibility(View.VISIBLE);
            holder.status_account_profile_boost_by.setVisibility(View.VISIBLE);
            holder.status_account_profile.setVisibility(View.GONE);
        }else{
            imageLoader.displayImage(ppurl, holder.status_account_profile, options);
            holder.status_account_profile_boost.setVisibility(View.GONE);
            holder.status_account_profile_boost_by.setVisibility(View.GONE);
            holder.status_account_profile.setVisibility(View.VISIBLE);
        }
        if( status.getReblog() == null) {
            if (status.getMedia_attachments().size() < 1) {
                holder.status_document_container.setVisibility(View.GONE);
                holder.status_show_more.setVisibility(View.GONE);
            } else {
                //If medias are loaded without any conditions or if device is on wifi
                if (!status.isSensitive() && (behaviorWithAttachments == Helper.ATTACHMENT_ALWAYS || (behaviorWithAttachments == Helper.ATTACHMENT_WIFI && isOnWifi))) {
                    loadAttachments(status);
                    holder.status_show_more.setVisibility(View.GONE);
                    status.setAttachmentShown(true);
                } else {
                    //Text depending if toots is sensitive or not
                    String textShowMore = (status.isSensitive()) ? context.getString(R.string.load_sensitive_attachment) : context.getString(R.string.load_attachment);
                    holder.status_show_more.setText(textShowMore);
                    if (!status.isAttachmentShown()) {
                        holder.status_show_more.setVisibility(View.VISIBLE);
                        holder.status_document_container.setVisibility(View.GONE);
                    } else {
                        loadAttachments(status);
                    }
                }
            }
        }else { //Attachments for reblogs
            if (status.getReblog().getMedia_attachments().size() < 1) {
                holder.status_document_container.setVisibility(View.GONE);
                holder.status_show_more.setVisibility(View.GONE);
            } else {
                //If medias are loaded without any conditions or if device is on wifi
                if (!status.getReblog().isSensitive() && (behaviorWithAttachments == Helper.ATTACHMENT_ALWAYS || (behaviorWithAttachments == Helper.ATTACHMENT_WIFI && isOnWifi))) {
                    loadAttachments(status.getReblog());
                    holder.status_show_more.setVisibility(View.GONE);
                    status.getReblog().setAttachmentShown(true);
                } else {
                    //Text depending if toots is sensitive or not
                    String textShowMore = (status.getReblog().isSensitive()) ? context.getString(R.string.load_sensitive_attachment) : context.getString(R.string.load_attachment);
                    holder.status_show_more.setText(textShowMore);
                    if (!status.isAttachmentShown()) {
                        holder.status_show_more.setVisibility(View.VISIBLE);
                        holder.status_document_container.setVisibility(View.GONE);
                    } else {
                        loadAttachments(status.getReblog());
                    }
                }
            }
        }

        switch (status.getVisibility()){
            case "public":
                holder.status_privacy.setImageResource(R.drawable.ic_action_globe);
                break;
            case "unlisted":
                holder.status_privacy.setImageResource(R.drawable.ic_action_lock_open);
                break;
            case "private":
                holder.status_privacy.setImageResource(R.drawable.ic_action_lock_closed);
                break;
            case "direct":
                holder.status_privacy.setImageResource(R.drawable.ic_local_post_office);
                break;
        }

        Drawable imgFav, imgReblog;
        if( status.isFavourited())
            imgFav = ContextCompat.getDrawable(context, R.drawable.ic_fav_yellow);
        else
            imgFav = ContextCompat.getDrawable(context, R.drawable.ic_fav_black);

        if( status.isReblogged())
            imgReblog = ContextCompat.getDrawable(context, R.drawable.ic_retweet_yellow);
        else
            imgReblog = ContextCompat.getDrawable(context, R.drawable.ic_retweet_black);

        imgFav.setBounds(0,0,(int) (20 * scale + 0.5f),(int) (20 * scale + 0.5f));
        imgReblog.setBounds(0,0,(int) (20 * scale + 0.5f),(int) (20 * scale + 0.5f));
        holder.status_favorite_count.setCompoundDrawables(imgFav, null, null, null);
        holder.status_reblog_count.setCompoundDrawables(imgReblog, null, null, null);

        holder.status_show_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadAttachments(status);
                holder.status_show_more.setVisibility(View.GONE);
                status.setAttachmentShown(true);
                statusListAdapter.notifyDataSetChanged();
            }
        });

        holder.status_favorite_count.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean confirmation = sharedpreferences.getBoolean(Helper.SET_NOTIF_VALIDATION, true);
                if( confirmation )
                    displayConfirmationDialog(FAVOURITE,status);
                else
                    favouriteAction(status);
            }
        });

        holder.status_reblog_count.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean confirmation = sharedpreferences.getBoolean(Helper.SET_NOTIF_VALIDATION, true);
                if( confirmation )
                    displayConfirmationDialog(REBLOG,status);
                else
                    reblogAction(status);
            }
        });
        switch (status.getVisibility()){
            case "direct":
            case "private":
                holder.status_reblog_count.setVisibility(View.GONE);
                break;
            case "public":
            case "unlisted":
                holder.status_reblog_count.setVisibility(View.VISIBLE);
                break;
            default:
                holder.status_reblog_count.setVisibility(View.VISIBLE);
        }

        holder.status_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moreOptionDialog(status);
            }
        });


        holder.status_account_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if( targetedId == null || !targetedId.equals(status.getAccount().getId())){
                    Intent intent = new Intent(context, ShowAccountActivity.class);
                    Bundle b = new Bundle();
                    b.putString("accountId", status.getAccount().getId());
                    intent.putExtras(b);
                    context.startActivity(intent);
                }
            }
        });

        holder.status_account_profile_boost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( targetedId == null || !targetedId.equals(status.getReblog().getAccount().getId())){
                    Intent intent = new Intent(context, ShowAccountActivity.class);
                    Bundle b = new Bundle();
                    b.putString("accountId", status.getReblog().getAccount().getId());
                    intent.putExtras(b);
                    context.startActivity(intent);
                }
            }
        });
        //Profile picture
        return convertView;
    }

    /**
     * Favourites/Unfavourites a status
     * @param status Status
     */
    private void favouriteAction(Status status){
        if( status.isFavourited()){
            new PostActionAsyncTask(context, API.StatusAction.UNFAVOURITE, status.getId(), StatusListAdapter.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            status.setFavourited(false);
        }else{
            new PostActionAsyncTask(context, API.StatusAction.FAVOURITE, status.getId(), StatusListAdapter.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            status.setFavourited(true);
        }
        statusListAdapter.notifyDataSetChanged();
    }

    /**
     * Reblog/Unreblog a status
     * @param status Status
     */
    private void reblogAction(Status status){
        if( status.isReblogged()){
            new PostActionAsyncTask(context, API.StatusAction.UNREBLOG, status.getId(), StatusListAdapter.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            status.setReblogged(false);
        }else{
            new PostActionAsyncTask(context, API.StatusAction.REBLOG, status.getId(), StatusListAdapter.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            status.setReblogged(true);
        }
        statusListAdapter.notifyDataSetChanged();
    }



    private void loadAttachments(final Status status){
        List<Attachment> attachments = status.getMedia_attachments();
        if( attachments != null && attachments.size() > 0){
            int i = 0;
            if( attachments.size() == 1){
                holder.status_container2.setVisibility(View.GONE);
                if( attachments.get(0).getUrl().trim().contains("missing.png"))
                    holder.status_document_container.setVisibility(View.GONE);
                else
                    holder.status_document_container.setVisibility(View.VISIBLE);
            }else if(attachments.size() == 2){
                holder.status_container2.setVisibility(View.VISIBLE);
                holder.status_container3.setVisibility(View.GONE);
                if( attachments.get(1).getUrl().trim().contains("missing.png"))
                    holder.status_container2.setVisibility(View.GONE);
                holder.status_document_container.setVisibility(View.VISIBLE);
            }else if( attachments.size() == 3){
                holder.status_container2.setVisibility(View.VISIBLE);
                holder.status_container3.setVisibility(View.VISIBLE);
                holder.status_prev4_container.setVisibility(View.GONE);
                if( attachments.get(2).getUrl().trim().contains("missing.png"))
                    holder.status_container3.setVisibility(View.GONE);
                holder.status_document_container.setVisibility(View.VISIBLE);
            }else {
                holder.status_prev4_container.setVisibility(View.VISIBLE);
                if( attachments.get(2).getUrl().trim().contains("missing.png"))
                    holder.status_prev4_container.setVisibility(View.GONE);
                holder.status_document_container.setVisibility(View.VISIBLE);
            }
            int position = 1;
            for(final Attachment attachment: attachments){
                ImageView imageView;
                if( i == 0) {
                    imageView = holder.status_prev1;
                    if( attachment.getType().equals("image"))
                        holder.status_prev1_play.setVisibility(View.GONE);
                    else
                        holder.status_prev1_play.setVisibility(View.VISIBLE);
                }else if( i == 1) {
                    imageView = holder.status_prev2;
                    if( attachment.getType().equals("image"))
                        holder.status_prev2_play.setVisibility(View.GONE);
                    else
                        holder.status_prev2_play.setVisibility(View.VISIBLE);
                }else if(i == 2) {
                    imageView = holder.status_prev3;
                    if( attachment.getType().equals("image"))
                        holder.status_prev3_play.setVisibility(View.GONE);
                    else
                        holder.status_prev3_play.setVisibility(View.VISIBLE);
                }else {
                    imageView = holder.status_prev4;
                    if( attachment.getType().equals("image"))
                        holder.status_prev4_play.setVisibility(View.GONE);
                    else
                        holder.status_prev4_play.setVisibility(View.VISIBLE);
                }
                String url = attachment.getPreview_url();
                if( url == null || url.trim().equals(""))
                    url = attachment.getUrl();
                if( !url.trim().contains("missing.png"))
                    imageLoader.displayImage(url, imageView, options);
                final int finalPosition = position;
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, MediaActivity.class);
                        Bundle b = new Bundle();
                        intent.putParcelableArrayListExtra("mediaArray", status.getMedia_attachments());
                        b.putInt("position", finalPosition);
                        intent.putExtras(b);
                        context.startActivity(intent);
                    }
                });
                i++;
                position++;
            }
        }else{
            holder.status_document_container.setVisibility(View.GONE);
        }
        holder.status_show_more.setVisibility(View.GONE);
    }

    @Override
    public void onPostAction(int statusCode, API.StatusAction statusAction, String targetedId, Error error) {

        if( error != null){
            final SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
            boolean show_error_messages = sharedpreferences.getBoolean(Helper.SET_SHOW_ERROR_MESSAGES, true);
            if( show_error_messages)
                Toast.makeText(context, error.getError(),Toast.LENGTH_LONG).show();
            return;
        }
        Helper.manageMessageStatusCode(context, statusCode, statusAction);
        //When muting or blocking an account, its status are removed from the list
        List<Status> statusesToRemove = new ArrayList<>();
        if( statusAction == API.StatusAction.MUTE || statusAction == API.StatusAction.BLOCK){
            for(Status status: statuses){
                if( status.getAccount().getId().equals(targetedId))
                    statusesToRemove.add(status);
            }
            statuses.removeAll(statusesToRemove);
            statusListAdapter.notifyDataSetChanged();
        }else  if( statusAction == API.StatusAction.UNSTATUS ){
            for(Status status: statuses){
                if( status.getId().equals(targetedId))
                    statusesToRemove.add(status);
            }
            statuses.removeAll(statusesToRemove);
            statusListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onTranslatedTextview(int position, String translatedResult, Boolean error) {
        if( error){
            Toast.makeText(context, R.string.toast_error_translate, Toast.LENGTH_LONG).show();
        }else if( statuses.size() > position) {
            try {
                String aJsonString = yandexTranslateToText(translatedResult);
                statuses.get(position).setTranslated(true);
                statuses.get(position).setTranslationShown(true);
                statuses.get(position).setContent_translated(aJsonString);
                statusListAdapter.notifyDataSetChanged();
            } catch (JSONException | UnsupportedEncodingException | IllegalArgumentException e) {
                Toast.makeText(context, R.string.toast_error_translate, Toast.LENGTH_LONG).show();
            }
        }
    }

    private String yandexTranslateToText(String text) throws JSONException, UnsupportedEncodingException{
        JSONObject translationJson = new JSONObject(text);
        JSONArray aJsonArray = translationJson.getJSONArray("text");
        String aJsonString = aJsonArray.get(0).toString();
        aJsonString = URLDecoder.decode(aJsonString, "UTF-8");
        return aJsonString;
    }

    private String googleTranslateToText(String text) throws JSONException, UnsupportedEncodingException{

        int i = 0;
        String aJsonString = "";
        while( i < new JSONArray(new JSONArray(text).get(0).toString()).length() ) {
            aJsonString += new JSONArray(new JSONArray(new JSONArray(text).get(0).toString()).get(i).toString()).get(0).toString();
            i++;
        }
        //Some fixes due to translation with Google
        aJsonString = aJsonString.trim();
        aJsonString = aJsonString.replace("< / ","</");
        aJsonString = aJsonString.replace("</ ","</");
        aJsonString = aJsonString.replace("> ",">");
        aJsonString = aJsonString.replace(" <","<");
        aJsonString = aJsonString.replace(" // ","//");
        aJsonString = aJsonString.replace("// ","//");
        aJsonString = aJsonString.replace(" //","//");
        aJsonString = aJsonString.replace(" www .","www.");
        aJsonString = aJsonString.replace("www .","www.");
        aJsonString = URLDecoder.decode(aJsonString, "UTF-8");
        return aJsonString;
    }

    private class ViewHolder {
        LinearLayout status_content_container;
        LinearLayout status_spoiler_container;
        TextView status_spoiler;
        Button status_spoiler_button;

        TextView status_content;
        TextView status_content_translated;
        LinearLayout status_content_translated_container;
        TextView status_account_username;
        TextView status_account_displayname;
        ImageView status_account_profile;
        ImageView status_account_profile_boost;
        ImageView status_account_profile_boost_by;
        TextView status_favorite_count;
        TextView status_reblog_count;
        TextView status_toot_date;
        TextView status_reblog_user;
        Button status_show_more;
        ImageView status_more;
        LinearLayout status_action_container;
        LinearLayout status_document_container;
        ImageView status_prev1;
        ImageView status_prev2;
        ImageView status_prev3;
        ImageView status_prev4;
        ImageView status_prev1_play;
        ImageView status_prev2_play;
        ImageView status_prev3_play;
        ImageView status_prev4_play;
        RelativeLayout status_prev4_container;
        ImageView status_reply;
        ImageView status_privacy;
        Button status_translate;
        LinearLayout status_container2;
        LinearLayout status_container3;
        LinearLayout main_container;
        TextView yandex_translate;
    }


    /**
     * Display a validation message
     * @param action int
     * @param status Status
     */
    private void displayConfirmationDialog(final int action, final Status status){

        String title = null;
        if( action == FAVOURITE){
            if( status.isFavourited())
                title = context.getString(R.string.favourite_remove);
            else
                title = context.getString(R.string.favourite_add);
        }else if( action == REBLOG ){
            if( status.isReblogged())
                title = context.getString(R.string.reblog_remove);
            else
                title = context.getString(R.string.reblog_add);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            builder.setMessage(Html.fromHtml(status.getContent(), Html.FROM_HTML_MODE_COMPACT));
        else
            //noinspection deprecation
            builder.setMessage(Html.fromHtml(status.getContent()));
        builder.setIcon(android.R.drawable.ic_dialog_alert)
            .setTitle(title)
            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if( action == REBLOG)
                        reblogAction(status);
                    else if( action == FAVOURITE)
                        favouriteAction(status);
                    dialog.dismiss();
                }
            })
            .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }

            })
            .show();
    }

    /**
     * More option for status (report / remove status / Mute / Block)
     * @param status Status current status
     */
    private void moreOptionDialog(final Status status){


        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        final boolean isOwner = status.getAccount().getId().equals(userId);
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(context);
        //builderSingle.setTitle(R.string.make_a_choice);
        final String[] stringArray, stringArrayConf;
        final API.StatusAction[] doAction;
        if( isOwner) {
            stringArray = context.getResources().getStringArray(R.array.more_action_owner);
            stringArrayConf = context.getResources().getStringArray(R.array.more_action_owner_confirm);
            doAction = new API.StatusAction[]{API.StatusAction.UNSTATUS};

        }else {
            stringArray = context.getResources().getStringArray(R.array.more_action);
            stringArrayConf = context.getResources().getStringArray(R.array.more_action_confirm);
            doAction = new API.StatusAction[]{API.StatusAction.MUTE,API.StatusAction.BLOCK,API.StatusAction.REPORT};
        }
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, stringArray);
        builderSingle.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                AlertDialog.Builder builderInner = new AlertDialog.Builder(context);
                builderInner.setTitle(stringArrayConf[which]);
                if( isOwner) {
                    if( which == 0) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                            builderInner.setMessage(Html.fromHtml(status.getContent(), Html.FROM_HTML_MODE_COMPACT));
                        else
                            //noinspection deprecation
                            builderInner.setMessage(Html.fromHtml(status.getContent()));
                    }else{
                        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                        String content;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                            content = Html.fromHtml(status.getContent(), Html.FROM_HTML_MODE_COMPACT).toString();
                        else
                            //noinspection deprecation
                            content = Html.fromHtml(status.getContent()).toString();
                        ClipData clip = ClipData.newPlainText(Helper.CLIP_BOARD, content);
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(context,R.string.clipboard,Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                        return;
                    }
                }else {
                    if( which < 2 ){
                        builderInner.setMessage(status.getAccount().getAcct());
                    }else if( which < 3) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                            builderInner.setMessage(Html.fromHtml(status.getContent(), Html.FROM_HTML_MODE_COMPACT));
                        else
                            //noinspection deprecation
                            builderInner.setMessage(Html.fromHtml(status.getContent()));
                    }else{
                        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                        String content;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                            content = Html.fromHtml(status.getContent(), Html.FROM_HTML_MODE_COMPACT).toString();
                        else
                            //noinspection deprecation
                            content = Html.fromHtml(status.getContent()).toString();
                        ClipData clip = ClipData.newPlainText(Helper.CLIP_BOARD, content);
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(context,R.string.clipboard,Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                        return;
                    }
                }
                //Text for report
                EditText input = null;
                final int position = which;
                if( doAction[which] == API.StatusAction.REPORT){
                    input = new EditText(context);
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
                    input.setLayoutParams(lp);
                    builderInner.setView(input);
                }
                builderInner.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,int which) {
                        dialog.dismiss();
                    }
                });
                final EditText finalInput = input;
                builderInner.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,int which) {
                        API.StatusAction statusAction = doAction[position];
                        if(statusAction ==  API.StatusAction.REPORT || statusAction == API.StatusAction.CREATESTATUS){
                            String comment = null;
                            if( finalInput != null && finalInput.getText() != null)
                                comment = finalInput.getText().toString();
                            new PostActionAsyncTask(context, statusAction, status.getId(), status, comment, StatusListAdapter.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        }else{
                            String targetedId;
                            if( doAction[position] == API.StatusAction.FAVOURITE ||
                                doAction[position] == API.StatusAction.UNFAVOURITE ||
                                doAction[position] == API.StatusAction.REBLOG ||
                                doAction[position] == API.StatusAction.UNREBLOG ||
                                doAction[position] == API.StatusAction.UNSTATUS
                            )
                                targetedId = status.getId();
                            else
                                targetedId = status.getAccount().getId();
                            new PostActionAsyncTask(context, statusAction, targetedId, StatusListAdapter.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        }
                        dialog.dismiss();
                    }
                });
                builderInner.show();
            }
        });
        builderSingle.create().requestWindowFeature(Window.FEATURE_NO_TITLE);
        builderSingle.show();
    }
}