package app.fedilab.android.fragments;
/* Copyright 2019 Thomas Schneider
 *
 * This file is a part of Fedilab
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 3 of the
 * License, or (at your option) any later version.
 *
 * Fedilab is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Fedilab; if not,
 * see <http://www.gnu.org/licenses>. */


import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.TimePickerDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.material.navigation.NavigationView;
import com.google.common.collect.ImmutableSet;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import app.fedilab.android.R;
import app.fedilab.android.activities.MainActivity;
import app.fedilab.android.activities.SettingsActivity;
import app.fedilab.android.animatemenu.interfaces.ScreenShotable;
import app.fedilab.android.asynctasks.DownloadTrackingDomainsAsyncTask;
import app.fedilab.android.asynctasks.UpdateAccountInfoAsyncTask;
import app.fedilab.android.client.Entities.Account;
import app.fedilab.android.client.Entities.Status;
import app.fedilab.android.filelister.FileListerDialog;
import app.fedilab.android.filelister.OnFileSelectedListener;
import app.fedilab.android.helper.Helper;
import app.fedilab.android.sqlite.AccountDAO;
import app.fedilab.android.sqlite.Sqlite;
import es.dmoral.toasty.Toasty;
import mabbas007.tagsedittext.TagsEditText;


import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;
import static app.fedilab.android.fragments.ContentSettingsFragment.type.ADMIN;
import static app.fedilab.android.fragments.ContentSettingsFragment.type.BATTERY;
import static app.fedilab.android.fragments.ContentSettingsFragment.type.COMPOSE;
import static app.fedilab.android.fragments.ContentSettingsFragment.type.INTERFACE;
import static app.fedilab.android.fragments.ContentSettingsFragment.type.NOTIFICATIONS;
import static app.fedilab.android.fragments.ContentSettingsFragment.type.TIMELINES;

public class ContentSettingsFragment  extends Fragment implements ScreenShotable {


    private View containerView;
    protected int res;
    private Bitmap bitmap;
    private type type;
    private Context context;

    public enum type{
        CLOSE,
        TIMELINES,
        ADMIN,
        NOTIFICATIONS,
        INTERFACE,
        COMPOSE,
        BATTERY,
    }

    private static final int ACTIVITY_CHOOSE_FILE = 411;
    private TextView set_folder;
    private EditText your_api_key;
    private int count1, count2, count3, count4, count5;
    private static final int ACTIVITY_CHOOSE_SOUND = 412;
    int count6 = 0;
    int count7 = 0;
    private int style;

    public static ContentSettingsFragment newInstance(int resId) {
        ContentSettingsFragment contentFragment = new ContentSettingsFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(Integer.class.getName(), resId);
        contentFragment.setArguments(bundle);
        return contentFragment;
    }

    @Override
    public void onViewCreated(@NotNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        context = getContext();
        assert context != null;
        this.containerView = view.findViewById(R.id.container);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) return;


        if(requestCode == ACTIVITY_CHOOSE_FILE) {
            Uri treeUri = data.getData();
            Uri docUri = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                docUri = DocumentsContract.buildDocumentUriUsingTree(treeUri,
                        DocumentsContract.getTreeDocumentId(treeUri));
            }
            try{
                String path = getPath(context, docUri);
                if( path == null )
                    path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
                final SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putString(Helper.SET_FOLDER_RECORD, path);
                editor.apply();
                set_folder.setText(path);
            }catch (Exception e){
                Toasty.error(context, context.getString(R.string.toast_error),Toast.LENGTH_LONG).show();
            }

        }
        if (requestCode == ACTIVITY_CHOOSE_SOUND){
            try{
                Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                final SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putString(Helper.SET_NOTIF_SOUND,  uri.toString());
                editor.apply();

            }catch (Exception e){
                Toasty.error(context, context.getString(R.string.toast_error),Toast.LENGTH_LONG).show();
            }
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        assert getArguments() != null;
        res = getArguments().getInt(Integer.class.getName());
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_settings_reveal, container, false);
        FrameLayout containerFrame = rootView.findViewById(R.id.container);

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            type = (type)bundle.getSerializable("type");
        }



        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, MODE_PRIVATE);
        int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);

        if (theme == Helper.THEME_DARK) {
            style = R.style.DialogDark;
        } else if (theme == Helper.THEME_BLACK){
            style = R.style.DialogBlack;
        }else {
            style = R.style.Dialog;
        }

        switch (theme){
            case Helper.THEME_LIGHT:
                containerFrame.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
                break;
            case Helper.THEME_DARK:
                containerFrame.setBackgroundColor(ContextCompat.getColor(context, R.color.mastodonC1));
                break;
            case Helper.THEME_BLACK:
                containerFrame.setBackgroundColor(ContextCompat.getColor(context, R.color.black));
                break;
            default:
                containerFrame.setBackgroundColor(ContextCompat.getColor(context, R.color.mastodonC1));
        }
        LinearLayout settings_timeline = rootView.findViewById(R.id.settings_timeline);
        LinearLayout settings_notifications = rootView.findViewById(R.id.settings_notifications);
        LinearLayout settings_admin = rootView.findViewById(R.id.settings_admin);
        LinearLayout settings_interface = rootView.findViewById(R.id.settings_interface);
        LinearLayout settings_compose = rootView.findViewById(R.id.settings_compose);
        LinearLayout settings_battery = rootView.findViewById(R.id.settings_battery);

        String title = "";
        if(type == null || type.equals(TIMELINES)){
            settings_timeline.setVisibility(View.VISIBLE);
            title = context.getString(R.string.timelines);
        }else if( type == NOTIFICATIONS){
            settings_notifications.setVisibility(View.VISIBLE);
            title = context.getString(R.string.notifications);
        }else if( type == ADMIN){
            settings_admin.setVisibility(View.VISIBLE);
            title = context.getString(R.string.administration);
        }else if(type == INTERFACE){
            settings_interface.setVisibility(View.VISIBLE);
            title = context.getString(R.string.u_interface);
        }else if(type == BATTERY){
            title = context.getString(R.string.battery);
            settings_battery.setVisibility(View.VISIBLE);
        }else if(type == COMPOSE){
            settings_compose.setVisibility(View.VISIBLE);
            title = context.getString(R.string.compose);
        }
        ((SettingsActivity) context)
                .setActionBarTitle(title);


        boolean auto_store = sharedpreferences.getBoolean(Helper.SET_AUTO_STORE, true);

        final CheckBox set_auto_store = rootView.findViewById(R.id.set_auto_store);
        set_auto_store.setChecked(auto_store);
        set_auto_store.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_AUTO_STORE, set_auto_store.isChecked());
                editor.apply();
            }
        });

        boolean display_content_after_fetch_more = sharedpreferences.getBoolean(Helper.SET_DISPLAY_CONTENT_AFTER_FM, true);
        final CheckBox set_display_content_after_fetch_more = rootView.findViewById(R.id.set_display_content_after_fetch_more);
        set_display_content_after_fetch_more.setChecked(display_content_after_fetch_more);
        set_display_content_after_fetch_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_DISPLAY_CONTENT_AFTER_FM, set_display_content_after_fetch_more.isChecked());
                editor.apply();
            }
        });

        count1 = 0;
        count2 = 0;
        count3 = 0;
        count4 = 0;
        count5 = 0;



        TagsEditText set_featured_tags = rootView.findViewById(R.id.set_featured_tags);
        if (MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.MASTODON){
            Set<String> featuredTagsSet = sharedpreferences.getStringSet(Helper.SET_FEATURED_TAGS, null);


            List<String> tags = new ArrayList<>();
            if( featuredTagsSet != null){
                tags = new ArrayList<>(featuredTagsSet);
            }
            String[] tagsString = tags.toArray(new String[tags.size()]);
            set_featured_tags.setTags(tagsString);

            set_featured_tags.setTagsListener(new TagsEditText.TagsEditListener() {
                @Override
                public void onTagsChanged(Collection<String> collection) {
                    Set<String> set = ImmutableSet.copyOf(collection);
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.putStringSet(Helper.SET_FEATURED_TAGS, set);
                    editor.apply();
                }

                @Override
                public void onEditingFinished() {

                }
            });
        }else{
            set_featured_tags.setVisibility(View.GONE);
        }




        Button update_tracking_domains = rootView.findViewById(R.id.update_tracking_domains);
        update_tracking_domains.setOnClickListener(v -> {
            new DownloadTrackingDomainsAsyncTask(getActivity().getApplicationContext(), update_tracking_domains).execute();
        });

        //Manage download of attachments
        RadioGroup radioGroup = rootView.findViewById(R.id.set_attachment_group);
        int attachmentAction = sharedpreferences.getInt(Helper.SET_ATTACHMENT_ACTION, Helper.ATTACHMENT_ALWAYS);
        switch (attachmentAction){
            case Helper.ATTACHMENT_ALWAYS:
                radioGroup.check(R.id.set_attachment_always);
                break;
            case Helper.ATTACHMENT_WIFI:
                radioGroup.check(R.id.set_attachment_wifi);
                break;
            case Helper.ATTACHMENT_ASK:
                radioGroup.check(R.id.set_attachment_ask);
                break;
        }
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch(checkedId) {
                    case R.id.set_attachment_always:
                        SharedPreferences.Editor editor = sharedpreferences.edit();
                        editor.putInt(Helper.SET_ATTACHMENT_ACTION, Helper.ATTACHMENT_ALWAYS);
                        editor.apply();
                        break;
                    case R.id.set_attachment_wifi:
                        editor = sharedpreferences.edit();
                        editor.putInt(Helper.SET_ATTACHMENT_ACTION, Helper.ATTACHMENT_WIFI);
                        editor.apply();
                        break;
                    case R.id.set_attachment_ask:
                        editor = sharedpreferences.edit();
                        editor.putInt(Helper.SET_ATTACHMENT_ACTION, Helper.ATTACHMENT_ASK);
                        editor.apply();
                        break;
                }
            }
        });



        int videoMode = sharedpreferences.getInt(Helper.SET_VIDEO_MODE, Helper.VIDEO_MODE_DIRECT);


        //Video mode
        final Spinner video_mode_spinner = rootView.findViewById(R.id.set_video_mode);
        ArrayAdapter<CharSequence> video_mode_spinnerAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.settings_video_mode, android.R.layout.simple_spinner_item);
        video_mode_spinner.setAdapter(video_mode_spinnerAdapter);
        if (videoMode == Helper.VIDEO_MODE_TORRENT)
            videoMode = Helper.VIDEO_MODE_DIRECT;
        int positionVideoMode = 0;
        if( videoMode == Helper.VIDEO_MODE_DIRECT)
            positionVideoMode = 1;
        video_mode_spinner.setSelection(positionVideoMode);
        video_mode_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if( count5 > 0 ) {
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    switch (position) {
                        /*case 0:
                            editor.putInt(Helper.SET_VIDEO_MODE, Helper.VIDEO_MODE_TORRENT);
                            editor.apply();
                            break;*/
                        case 0:
                            editor.putInt(Helper.SET_VIDEO_MODE, Helper.VIDEO_MODE_WEBVIEW);
                            editor.apply();
                            break;
                        case 1:
                            editor.putInt(Helper.SET_VIDEO_MODE, Helper.VIDEO_MODE_DIRECT);
                            editor.apply();
                            break;
                    }
                }
                count5++;
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        boolean patch_provider = sharedpreferences.getBoolean(Helper.SET_SECURITY_PROVIDER, true);
        final CheckBox set_security_provider = rootView.findViewById(R.id.set_security_provider);
        set_security_provider.setChecked(patch_provider);

        set_security_provider.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_SECURITY_PROVIDER, set_security_provider.isChecked());
                editor.apply();
            }
        });


        boolean display_card = sharedpreferences.getBoolean(Helper.SET_DISPLAY_CARD, false);

        final CheckBox set_display_card = rootView.findViewById(R.id.set_display_card);
        set_display_card.setChecked(display_card);
        set_display_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_DISPLAY_CARD, set_display_card.isChecked());
                editor.apply();
            }
        });

        String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        String instance = sharedpreferences.getString(Helper.PREF_INSTANCE, Helper.getLiveInstance(context));

        boolean display_admin_menu = sharedpreferences.getBoolean(Helper.SET_DISPLAY_ADMIN_MENU + userId + instance, false);

        final CheckBox set_display_admin_menu = rootView.findViewById(R.id.set_display_admin_menu);
        set_display_admin_menu.setChecked(display_admin_menu);
        set_display_admin_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_DISPLAY_ADMIN_MENU + userId + instance, set_display_admin_menu.isChecked());
                editor.apply();
                Bundle b = new Bundle();
                if( set_display_admin_menu.isChecked()){
                    b.putString("menu", "show_admin");
                }else{
                    b.putString("menu", "hide_admin");
                }
                Intent intentBC = new Intent(Helper.RECEIVE_HIDE_ITEM);
                intentBC.putExtras(b);
                LocalBroadcastManager.getInstance(context).sendBroadcast(intentBC);
            }
        });

        boolean display_admin_statuses = sharedpreferences.getBoolean(Helper.SET_DISPLAY_ADMIN_STATUSES + userId + instance, false);

        final CheckBox set_display_admin_statuses = rootView.findViewById(R.id.set_display_admin_statuses);
        set_display_admin_statuses.setChecked(display_admin_statuses);
        set_display_admin_statuses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_DISPLAY_ADMIN_STATUSES + userId + instance, set_display_admin_statuses.isChecked());
                editor.apply();
            }
        });

        boolean show_media_urls = sharedpreferences.getBoolean(Helper.SET_MEDIA_URLS, false);
        final CheckBox set_auto_add_media_url = rootView.findViewById(R.id.set_auto_add_media_url);
        set_auto_add_media_url.setChecked(show_media_urls);

        set_auto_add_media_url.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_MEDIA_URLS, set_auto_add_media_url.isChecked());
                editor.apply();
            }
        });

        boolean display_video_preview = sharedpreferences.getBoolean(Helper.SET_DISPLAY_VIDEO_PREVIEWS, true);
        final CheckBox set_display_video_preview = rootView.findViewById(R.id.set_display_video_preview);
        set_display_video_preview.setChecked(display_video_preview);

        set_display_video_preview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_DISPLAY_VIDEO_PREVIEWS, set_display_video_preview.isChecked());
                editor.apply();
            }
        });


        boolean notif_validation = sharedpreferences.getBoolean(Helper.SET_NOTIF_VALIDATION, true);
        final CheckBox set_share_validation = rootView.findViewById(R.id.set_share_validation);
        set_share_validation.setChecked(notif_validation);

        set_share_validation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_NOTIF_VALIDATION, set_share_validation.isChecked());
                editor.apply();
            }
        });
        your_api_key = rootView.findViewById(R.id.translation_key);

        your_api_key.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                int translatore = sharedpreferences.getInt(Helper.SET_TRANSLATOR, Helper.TRANS_YANDEX);
                String store = null;
                if( translatore == Helper.TRANS_YANDEX)
                    store = Helper.SET_YANDEX_API_KEY;
                else if( translatore == Helper.TRANS_DEEPL)
                    store = Helper.SET_DEEPL_API_KEY;
                if( store != null)
                    if( s != null && s.length() > 0)
                        editor.putString(store, s.toString().trim());
                    else
                        editor.putString(store, null);
                editor.apply();
            }
        });

        boolean notif_validation_fav = sharedpreferences.getBoolean(Helper.SET_NOTIF_VALIDATION_FAV, false);
        final CheckBox set_share_validation_fav = rootView.findViewById(R.id.set_share_validation_fav);
        set_share_validation_fav.setChecked(notif_validation_fav);

        set_share_validation_fav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_NOTIF_VALIDATION_FAV, set_share_validation_fav.isChecked());
                editor.apply();
            }
        });


        boolean expand_cw = sharedpreferences.getBoolean(Helper.SET_EXPAND_CW, false);
        final CheckBox set_expand_cw = rootView.findViewById(R.id.set_expand_cw);
        set_expand_cw.setChecked(expand_cw);

        set_expand_cw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_EXPAND_CW, set_expand_cw.isChecked());
                editor.apply();
            }
        });

        boolean display_emoji = sharedpreferences.getBoolean(Helper.SET_DISPLAY_EMOJI, true);
        final CheckBox set_display_emoji = rootView.findViewById(R.id.set_display_emoji);
        set_display_emoji.setChecked(display_emoji);

        set_display_emoji.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_DISPLAY_EMOJI, set_display_emoji.isChecked());
                editor.apply();
            }
        });

        boolean expand_media = sharedpreferences.getBoolean(Helper.SET_EXPAND_MEDIA, false);
        final CheckBox set_expand_media = rootView.findViewById(R.id.set_expand_image);
        set_expand_media.setChecked(expand_media);

        set_expand_media.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_EXPAND_MEDIA, set_expand_media.isChecked());
                editor.apply();
            }
        });


        boolean photo_editor = sharedpreferences.getBoolean(Helper.SET_PHOTO_EDITOR, true);
        final CheckBox set_photo_editor = rootView.findViewById(R.id.set_photo_editor);
        set_photo_editor.setChecked(photo_editor);

        set_photo_editor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_PHOTO_EDITOR, set_photo_editor.isChecked());
                editor.apply();
            }
        });

        int upload_img_max_retry_times = sharedpreferences.getInt(Helper.MAX_UPLOAD_IMG_RETRY_TIMES, 3);
        final SeekBar max_times_bar = rootView.findViewById(R.id.max_upload_image_retry_times);
        final TextView max_times_value = rootView.findViewById(R.id.max_upload_image_retry_times_value);
        max_times_bar.setProgress(upload_img_max_retry_times);
        max_times_value.setText(String.valueOf(upload_img_max_retry_times));
        max_times_bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                max_times_value.setText(String.valueOf(progress));
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putInt(Helper.MAX_UPLOAD_IMG_RETRY_TIMES, progress);
                editor.apply();
            }
        });
        boolean remember_position_home = sharedpreferences.getBoolean(Helper.SET_REMEMBER_POSITION_HOME, true);
        final CheckBox set_remember_position = rootView.findViewById(R.id.set_remember_position);
        set_remember_position.setChecked(remember_position_home);

        set_remember_position.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_REMEMBER_POSITION_HOME, set_remember_position.isChecked());
                editor.apply();
            }
        });



        boolean hide_delete_notification_on_tab = sharedpreferences.getBoolean(Helper.SET_HIDE_DELETE_BUTTON_ON_TAB, false);
        final CheckBox set_hide_delete_notification_on_tab = rootView.findViewById(R.id.set_hide_delete_notification_on_tab);
        set_hide_delete_notification_on_tab.setChecked(hide_delete_notification_on_tab);

        set_hide_delete_notification_on_tab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_HIDE_DELETE_BUTTON_ON_TAB, set_hide_delete_notification_on_tab.isChecked());
                editor.apply();
                if( getActivity() != null)
                    getActivity().recreate();
                Intent intent = new Intent(context, MainActivity.class);
                if(getActivity() != null)
                    getActivity().finish();
                startActivity(intent);
            }
        });


        boolean blur_sensitive = sharedpreferences.getBoolean(Helper.SET_BLUR_SENSITIVE, true);
        final CheckBox set_blur_sensitive = rootView.findViewById(R.id.set_blur_sensitive);
        set_blur_sensitive.setChecked(blur_sensitive);

        set_blur_sensitive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_BLUR_SENSITIVE, set_blur_sensitive.isChecked());
                editor.apply();
            }
        });

        boolean long_press_media = sharedpreferences.getBoolean(Helper.SET_LONG_PRESS_MEDIA, true);
        final CheckBox set_long_press_media = rootView.findViewById(R.id.set_long_press_media);
        set_long_press_media.setChecked(long_press_media);

        set_long_press_media.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_LONG_PRESS_MEDIA, set_long_press_media.isChecked());
                editor.apply();
            }
        });


        boolean display_timeline_in_list = sharedpreferences.getBoolean(Helper.SET_DISPLAY_TIMELINE_IN_LIST, false);
        final CheckBox set_display_timeline_in_list = rootView.findViewById(R.id.set_display_timeline_in_list);
        set_display_timeline_in_list.setChecked(display_timeline_in_list);

        set_display_timeline_in_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_DISPLAY_TIMELINE_IN_LIST, set_display_timeline_in_list.isChecked());
                editor.apply();
                Bundle b = new Bundle();
                if( set_display_timeline_in_list.isChecked()){
                    b.putString("menu", "show_list_button");
                }else{
                    b.putString("menu", "hide_list_button");
                }
                Intent intentBC = new Intent(Helper.RECEIVE_HIDE_ITEM);
                intentBC.putExtras(b);
                LocalBroadcastManager.getInstance(context).sendBroadcast(intentBC);
            }
        });


        boolean display_news = sharedpreferences.getBoolean(Helper.SET_DISPLAY_NEWS_FROM_FEDILAB, false);
        final CheckBox set_display_news = rootView.findViewById(R.id.set_display_news);
        set_display_news.setChecked(display_news);

        set_display_news.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_DISPLAY_NEWS_FROM_FEDILAB, set_display_news.isChecked());
                editor.apply();
                Bundle b = new Bundle();
                if( set_display_news.isChecked()){
                    b.putString("menu", "show_news");
                }else{
                    b.putString("menu", "hide_news");
                }
                Intent intentBC = new Intent(Helper.RECEIVE_HIDE_ITEM);
                intentBC.putExtras(b);
                LocalBroadcastManager.getInstance(context).sendBroadcast(intentBC);
            }
        });

        int truncate_toots_size = sharedpreferences.getInt(Helper.SET_TRUNCATE_TOOTS_SIZE, 0);
        SeekBar set_truncate_size = rootView.findViewById(R.id.set_truncate_size);
        set_truncate_size.setMax(20);
        set_truncate_size.setProgress(truncate_toots_size);
        TextView set_truncate_toots = rootView.findViewById(R.id.set_truncate_toots);
        set_truncate_toots.setText(String.valueOf(truncate_toots_size));
        set_truncate_size.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                set_truncate_toots.setText(String.valueOf(progress));
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putInt(Helper.SET_TRUNCATE_TOOTS_SIZE, progress);
                editor.apply();
            }
        });



        boolean new_badge = sharedpreferences.getBoolean(Helper.SET_DISPLAY_NEW_BADGE, true);
        final CheckBox set_new_badge = rootView.findViewById(R.id.set_display_new_badge);
        set_new_badge.setChecked(new_badge);

        set_new_badge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_DISPLAY_NEW_BADGE, set_new_badge.isChecked());
                editor.apply();
            }
        });

        boolean fedilab_features_button = sharedpreferences.getBoolean(Helper.SET_DISPLAY_FEDILAB_FEATURES_BUTTON, true);
        final CheckBox set_fedilab_features_button = rootView.findViewById(R.id.set_display_fedilab_features_button);
        set_fedilab_features_button.setChecked(fedilab_features_button);

        set_fedilab_features_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_DISPLAY_FEDILAB_FEATURES_BUTTON, set_fedilab_features_button.isChecked());
                editor.apply();
            }
        });

        boolean bot_icon = sharedpreferences.getBoolean(Helper.SET_DISPLAY_BOT_ICON, true);
        final CheckBox set_bot_icon = rootView.findViewById(R.id.set_display_bot_icon);
        set_bot_icon.setChecked(bot_icon);

        set_bot_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_DISPLAY_BOT_ICON, set_bot_icon.isChecked());
                editor.apply();
            }
        });

        boolean display_confirm = sharedpreferences.getBoolean(Helper.SET_DISPLAY_CONFIRM, true);
        final CheckBox set_display_confirm = rootView.findViewById(R.id.set_display_confirm);
        set_display_confirm.setChecked(display_confirm);

        set_display_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_DISPLAY_CONFIRM, set_display_confirm.isChecked());
                editor.apply();
            }
        });

        boolean quick_reply = sharedpreferences.getBoolean(Helper.SET_QUICK_REPLY, true);
        final CheckBox set_quick_reply = rootView.findViewById(R.id.set_quick_reply);
        set_quick_reply.setChecked(quick_reply);

        set_quick_reply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_QUICK_REPLY, set_quick_reply.isChecked());
                editor.apply();
            }
        });

        boolean fit_preview = sharedpreferences.getBoolean(Helper.SET_FULL_PREVIEW, false);
        final CheckBox set_fit_preview = rootView.findViewById(R.id.set_fit_preview);
        set_fit_preview.setChecked(fit_preview);

        set_fit_preview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_FULL_PREVIEW, set_fit_preview.isChecked());
                editor.apply();
            }
        });

        boolean compact_mode = sharedpreferences.getBoolean(Helper.SET_COMPACT_MODE, false);
        boolean console_mode = sharedpreferences.getBoolean(Helper.SET_CONSOLE_MODE, false);
        RadioGroup set_mode = rootView.findViewById(R.id.set_mode);
        if( compact_mode){
            set_mode.check(R.id.set_compact_mode);
        }else if(console_mode){
            set_mode.check(R.id.set_console_mode);
        }else {
            set_mode.check(R.id.set_normal_mode);
        }
        set_mode.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch(checkedId) {
                    case R.id.set_compact_mode:
                        SharedPreferences.Editor editor = sharedpreferences.edit();
                        editor.putBoolean(Helper.SET_COMPACT_MODE, true);
                        editor.putBoolean(Helper.SET_CONSOLE_MODE, false);
                        editor.apply();
                        break;
                    case R.id.set_console_mode:
                        editor = sharedpreferences.edit();
                        editor.putBoolean(Helper.SET_COMPACT_MODE, false);
                        editor.putBoolean(Helper.SET_CONSOLE_MODE, true);
                        editor.apply();
                        break;
                    case R.id.set_normal_mode:
                        editor = sharedpreferences.edit();
                        editor.putBoolean(Helper.SET_COMPACT_MODE, false);
                        editor.putBoolean(Helper.SET_CONSOLE_MODE, false);
                        editor.apply();
                        break;
                }
            }
        });

        boolean share_details = sharedpreferences.getBoolean(Helper.SET_SHARE_DETAILS, true);
        final CheckBox set_share_details = rootView.findViewById(R.id.set_share_details);
        set_share_details.setChecked(share_details);

        set_share_details.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_SHARE_DETAILS, set_share_details.isChecked());
                editor.apply();
            }
        });

        // retrieve metadata if URL from external apps when composing
        boolean should_retrieve_metadata = sharedpreferences.getBoolean(Helper.SET_RETRIEVE_METADATA_IF_URL_FROM_EXTERAL, true);
        final CheckBox set_retrieve_metadata = rootView.findViewById(R.id.set_retrieve_metadata_share_from_extras);
        set_retrieve_metadata.setChecked(should_retrieve_metadata);

        set_retrieve_metadata.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_RETRIEVE_METADATA_IF_URL_FROM_EXTERAL, set_retrieve_metadata.isChecked());
                editor.apply();
            }
        });

        // Custom Sharing
        final EditText edit_custom_sharing_url = rootView.findViewById(R.id.custom_sharing_url);
        boolean custom_sharing = sharedpreferences.getBoolean(Helper.SET_CUSTOM_SHARING, false);
        final CheckBox set_custom_sharing = rootView.findViewById(R.id.set_custom_sharing);
        set_custom_sharing.setChecked(custom_sharing);
        if( custom_sharing)
            edit_custom_sharing_url.setVisibility(View.VISIBLE);
        else
            edit_custom_sharing_url.setVisibility(View.GONE);
        set_custom_sharing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_CUSTOM_SHARING, set_custom_sharing.isChecked());
                editor.apply();
                if( set_custom_sharing.isChecked())
                    edit_custom_sharing_url.setVisibility(View.VISIBLE);
                else
                    edit_custom_sharing_url.setVisibility(View.GONE);
            }
        });

        // forward tags in replies
        boolean forward_tags = sharedpreferences.getBoolean(Helper.SET_FORWARD_TAGS_IN_REPLY, false);
        final CheckBox set_forward_tags = rootView.findViewById(R.id.set_forward_tags);
        set_forward_tags.setChecked(forward_tags);
        set_forward_tags.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_FORWARD_TAGS_IN_REPLY, set_forward_tags.isChecked());
                editor.apply();
            }
        });

        String custom_sharing_url = sharedpreferences.getString(Helper.SET_CUSTOM_SHARING_URL,"");
        if (custom_sharing_url.equals("")) {
            custom_sharing_url = "http://cs.example.net/add?token=umVe1zurZk47ihElSOQcBG05KUSA2v-GSet4_fFnJ4M&url=${url}&title=${title}&source=${source}&id=${id}&description=${description}&keywords=${keywords}&creator=${creator}&thumbnailurl=${thumbnailurl}";
        }
        edit_custom_sharing_url.setText(custom_sharing_url);


        edit_custom_sharing_url.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putString(Helper.SET_CUSTOM_SHARING_URL, s.toString().trim());
                editor.apply();
            }
        });

        boolean disableGif = sharedpreferences.getBoolean(Helper.SET_DISABLE_GIF, false);
        final CheckBox set_disable_gif = rootView.findViewById(R.id.set_disable_gif);
        set_disable_gif.setChecked(disableGif);
        set_disable_gif.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_DISABLE_GIF, set_disable_gif.isChecked());
                editor.apply();
                if( getActivity() != null)
                    getActivity().recreate();
            }
        });


        boolean livenotif = sharedpreferences.getBoolean(Helper.SET_LIVE_NOTIFICATIONS, true);
        final CheckBox set_live_notif = rootView.findViewById(R.id.set_live_notify);
        set_live_notif.setChecked(livenotif);
        set_live_notif.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_LIVE_NOTIFICATIONS, set_live_notif.isChecked());
                editor.putBoolean(Helper.SHOULD_CONTINUE_STREAMING, set_live_notif.isChecked());
                editor.apply();
                if( set_live_notif.isChecked() ){
                    try {
                        ((MainActivity) context).startSreaming();
                    }catch (Exception ignored){ignored.printStackTrace();}
                }else{
                    context.sendBroadcast(new Intent("StopLiveNotificationService"));
                }
            }
        });

        boolean keep_background_process = sharedpreferences.getBoolean(Helper.SET_KEEP_BACKGROUND_PROCESS, true);
        final CheckBox set_keep_background_process = rootView.findViewById(R.id.set_keep_background_process);
        set_keep_background_process.setChecked(keep_background_process);
        set_keep_background_process.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_KEEP_BACKGROUND_PROCESS, set_keep_background_process.isChecked());
                editor.apply();
            }
        });


        boolean capitalize = sharedpreferences.getBoolean(Helper.SET_CAPITALIZE, true);
        final CheckBox set_capitalize = rootView.findViewById(R.id.set_capitalize);
        set_capitalize.setChecked(capitalize);

        set_capitalize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_CAPITALIZE, set_capitalize.isChecked());
                editor.apply();
            }
        });


        final CheckBox set_embedded_browser = rootView.findViewById(R.id.set_embedded_browser);
        final LinearLayout set_javascript_container = rootView.findViewById(R.id.set_javascript_container);
        final CheckBox set_custom_tabs  = rootView.findViewById(R.id.set_custom_tabs);
        final  LinearLayout custom_tabs_container  = rootView.findViewById(R.id.custom_tabs_container);
        final SwitchCompat set_javascript = rootView.findViewById(R.id.set_javascript);
        boolean javascript = sharedpreferences.getBoolean(Helper.SET_JAVASCRIPT, true);
        boolean embedded_browser = sharedpreferences.getBoolean(Helper.SET_EMBEDDED_BROWSER, true);
        boolean custom_tabs = sharedpreferences.getBoolean(Helper.SET_CUSTOM_TABS, true);
        if( !embedded_browser){
            set_javascript_container.setVisibility(View.GONE);
            custom_tabs_container.setVisibility(View.VISIBLE);
        }else{
            set_javascript_container.setVisibility(View.VISIBLE);
            custom_tabs_container.setVisibility(View.GONE);
        }
        set_embedded_browser.setChecked(embedded_browser);
        set_embedded_browser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_EMBEDDED_BROWSER, set_embedded_browser.isChecked());
                editor.apply();
                if( !set_embedded_browser.isChecked()){
                    set_javascript_container.setVisibility(View.GONE);
                    custom_tabs_container.setVisibility(View.VISIBLE);
                }else{
                    set_javascript_container.setVisibility(View.VISIBLE);
                    custom_tabs_container.setVisibility(View.GONE);
                }
            }
        });

        set_javascript.setChecked(javascript);
        set_javascript.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_JAVASCRIPT, isChecked);
                editor.apply();
            }
        });

        set_custom_tabs.setChecked(custom_tabs);
        set_custom_tabs.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_CUSTOM_TABS, isChecked);
                editor.apply();
            }
        });

        final LinearLayout set_cookies_container = rootView.findViewById(R.id.set_cookies_container);
        final SwitchCompat set_cookies = rootView.findViewById(R.id.set_cookies);
        boolean cookies = sharedpreferences.getBoolean(Helper.SET_COOKIES, false);

        set_cookies.setChecked(cookies);
        set_cookies.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_COOKIES, isChecked);
                editor.apply();
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            set_cookies_container.setVisibility(View.VISIBLE);
        }else {
            set_cookies_container.setVisibility(View.GONE);
        }
        final String targeted_folder = sharedpreferences.getString(Helper.SET_FOLDER_RECORD, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath());

        set_folder = rootView.findViewById(R.id.set_folder);
        set_folder.setText(targeted_folder);
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            set_folder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    @SuppressLint("InlinedApi") Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                    startActivityForResult(intent, ACTIVITY_CHOOSE_FILE);
                }
            });
        }else {
            LinearLayout file_chooser = rootView.findViewById(R.id.file_chooser);
            file_chooser.setVisibility(View.GONE);
        }*/
        set_folder.setOnClickListener(view ->{
            FileListerDialog fileListerDialog = FileListerDialog.createFileListerDialog(context, style);
            fileListerDialog.setDefaultDir(targeted_folder);
            fileListerDialog.setFileFilter(FileListerDialog.FILE_FILTER.DIRECTORY_ONLY);
            fileListerDialog.setOnFileSelectedListener(new OnFileSelectedListener() {
                @Override
                public void onFileSelected(File file, String path) {
                    if( path == null )
                        path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
                    final SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.putString(Helper.SET_FOLDER_RECORD, path);
                    editor.apply();
                    set_folder.setText(path);
                }
            });
            fileListerDialog.show();
        });

        final Spinner set_night_mode = rootView.findViewById(R.id.set_night_mode);
        ArrayAdapter<CharSequence> adapterTheme = ArrayAdapter.createFromResource(getContext(),
                R.array.settings_theme, android.R.layout.simple_spinner_item);
        set_night_mode.setAdapter(adapterTheme);

        int positionSpinnerTheme;
        switch (sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK)){
            case Helper.THEME_DARK:
                positionSpinnerTheme = 0;
                break;
            case Helper.THEME_LIGHT:
                positionSpinnerTheme = 1;
                break;
            case Helper.THEME_BLACK:
                positionSpinnerTheme = 2;
                break;
            default:
                positionSpinnerTheme = 0;
        }
        set_night_mode.setSelection(positionSpinnerTheme);
        set_night_mode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if( count1 > 0 ) {
                    SharedPreferences.Editor editor = sharedpreferences.edit();

                    switch (position) {
                        case 0:
                            editor.putInt(Helper.SET_THEME, Helper.THEME_DARK);
                            editor.apply();
                            break;
                        case 1:
                            editor.putInt(Helper.SET_THEME, Helper.THEME_LIGHT);
                            editor.apply();
                            break;
                        case 2:
                            editor.putInt(Helper.SET_THEME, Helper.THEME_BLACK);
                            editor.apply();
                            break;
                    }
                    Bundle b = new Bundle();
                    b.putString("menu", "theme");
                    Intent intentBC = new Intent(Helper.RECEIVE_HIDE_ITEM);
                    intentBC.putExtras(b);
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intentBC);
                    ((SettingsActivity) context).recreate();
                }
                count1++;
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });



        // NSFW Timeout
        SeekBar nsfwTimeoutSeekBar = rootView.findViewById(R.id.set_nsfw_timeout);
        final TextView set_nsfw_timeout_value = rootView.findViewById(R.id.set_nsfw_timeout_value);

        nsfwTimeoutSeekBar.setMax(30);

        int nsfwTimeout = sharedpreferences.getInt(Helper.SET_NSFW_TIMEOUT, 5);

        nsfwTimeoutSeekBar.setProgress(nsfwTimeout);
        set_nsfw_timeout_value.setText(String.valueOf(nsfwTimeout));

        nsfwTimeoutSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                set_nsfw_timeout_value.setText(String.valueOf(progress));

                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putInt(Helper.SET_NSFW_TIMEOUT, progress);
                editor.apply();
            }
        });





        LinearLayout toot_visibility_container = rootView.findViewById(R.id.toot_visibility_container);
        SQLiteDatabase db = Sqlite.getInstance(context, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();

        final Account account = new AccountDAO(context, db).getUniqAccount(userId, instance);
        final ImageView set_toot_visibility = rootView.findViewById(R.id.set_toot_visibility);
        if( theme == Helper.THEME_DARK){
            Helper.changeDrawableColor(context, set_toot_visibility, R.color.dark_text);
        }else {
            Helper.changeDrawableColor(context, set_toot_visibility, R.color.white);
        }
        //Only displayed for non locked accounts
        if (account != null ) {
            String defaultVisibility = account.isLocked()?"private":"public";
            String tootVisibility = sharedpreferences.getString(Helper.SET_TOOT_VISIBILITY + "@" + account.getAcct() + "@" + account.getInstance(), defaultVisibility);
            switch (tootVisibility) {
                case "public":
                    set_toot_visibility.setImageResource(R.drawable.ic_public);
                    break;
                case "unlisted":
                    set_toot_visibility.setImageResource(R.drawable.ic_lock_open);
                    break;
                case "private":
                    set_toot_visibility.setImageResource(R.drawable.ic_lock_outline);
                    break;
                case "direct":
                    set_toot_visibility.setImageResource(R.drawable.ic_mail_outline);
                    break;
            }
        }else {
            toot_visibility_container.setVisibility(View.GONE);
        }

        set_toot_visibility.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);

                AlertDialog.Builder dialog = new AlertDialog.Builder(context, style);
                dialog.setTitle(R.string.toot_visibility_tilte);
                final String[] stringArray = getResources().getStringArray(R.array.toot_visibility);
                final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, stringArray);
                dialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int position) {
                        dialog.dismiss();
                    }
                });

                dialog.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int position) {
                        String visibility = "public";

                        switch (position){
                            case 0:
                                visibility = "public";
                                set_toot_visibility.setImageResource(R.drawable.ic_public);
                                break;
                            case 1:
                                visibility = "unlisted";
                                set_toot_visibility.setImageResource(R.drawable.ic_lock_open);
                                break;
                            case 2:
                                visibility = "private";
                                set_toot_visibility.setImageResource(R.drawable.ic_lock_outline);
                                break;
                            case 3:
                                visibility = "direct";
                                set_toot_visibility.setImageResource(R.drawable.ic_mail_outline);
                                break;
                        }
                        if( account != null) {
                            SharedPreferences.Editor editor = sharedpreferences.edit();
                            editor.putString(Helper.SET_TOOT_VISIBILITY + "@" + account.getAcct() + "@" + account.getInstance(), visibility);
                            editor.apply();
                            Toasty.info(context, context.getString(R.string.toast_visibility_changed, "@" + account.getAcct() + "@" + account.getInstance()), Toast.LENGTH_SHORT).show();
                        }else {
                            Toasty.error(context, context.getString(R.string.toast_error),Toast.LENGTH_LONG).show();
                        }

                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        });


        boolean trans_forced = sharedpreferences.getBoolean(Helper.SET_TRANS_FORCED, false);
        final CheckBox set_trans_forced = rootView.findViewById(R.id.set_trans_forced);
        set_trans_forced.setChecked(trans_forced);
        set_trans_forced.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_TRANS_FORCED, set_trans_forced.isChecked());
                editor.apply();
            }
        });

        int split_size_val = sharedpreferences.getInt(Helper.SET_AUTOMATICALLY_SPLIT_TOOTS_SIZE+userId+instance, Helper.SPLIT_TOOT_SIZE);

        LinearLayout set_split_container = rootView.findViewById(R.id.set_split_container);
        //split size
        SeekBar split_size = rootView.findViewById(R.id.set_split_size);
        final TextView split_text = rootView.findViewById(R.id.set_split_text);

        split_size.setProgress(0);
        split_text.setText(String.valueOf(split_size_val));
        split_size.setMax(5);
        split_size.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int newProgress = (progress + 1) * Helper.SPLIT_TOOT_SIZE;
                split_text.setText(String.valueOf(newProgress));
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putInt(Helper.SET_AUTOMATICALLY_SPLIT_TOOTS_SIZE+userId+instance, newProgress);
                editor.apply();
            }
        });

        boolean split_toot = sharedpreferences.getBoolean(Helper.SET_AUTOMATICALLY_SPLIT_TOOTS+userId+instance, false);
        if( !split_toot){
            set_split_container.setVisibility(View.GONE);
        }
        final CheckBox set_split_toot = rootView.findViewById(R.id.set_automatically_split_toot);
        set_split_toot.setChecked(split_toot);
        set_split_toot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_AUTOMATICALLY_SPLIT_TOOTS+userId+instance, set_split_toot.isChecked());
                editor.apply();
                if( set_split_toot.isChecked()){
                    set_split_container.setVisibility(View.VISIBLE);
                }else{
                    set_split_container.setVisibility(View.GONE);
                }
            }
        });

        //Translators
        final Spinner translation_layout_spinner = rootView.findViewById(R.id.translation_layout_spinner);
        ArrayAdapter<CharSequence> adapterTrans = ArrayAdapter.createFromResource(getContext(),
                R.array.settings_translation, android.R.layout.simple_spinner_item);
        translation_layout_spinner.setAdapter(adapterTrans);

        int positionSpinnerTrans;
        switch (sharedpreferences.getInt(Helper.SET_TRANSLATOR, Helper.TRANS_YANDEX)){
            case Helper.TRANS_YANDEX:
                positionSpinnerTrans = 0;
                your_api_key.setVisibility(View.VISIBLE);
                your_api_key.setText(sharedpreferences.getString(Helper.SET_YANDEX_API_KEY, ""));
                break;
            case Helper.TRANS_DEEPL:
                positionSpinnerTrans = 1;
                your_api_key.setVisibility(View.VISIBLE);
                your_api_key.setText(sharedpreferences.getString(Helper.SET_DEEPL_API_KEY, ""));
                break;
            case Helper.TRANS_NONE:
                positionSpinnerTrans = 2;
                your_api_key.setVisibility(View.GONE);
                break;
            default:
                your_api_key.setVisibility(View.VISIBLE);
                positionSpinnerTrans = 0;
        }
        translation_layout_spinner.setSelection(positionSpinnerTrans);
        translation_layout_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if( count3 > 0 ) {
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    switch (position) {
                        case 0:
                            your_api_key.setVisibility(View.VISIBLE);
                            editor.putInt(Helper.SET_TRANSLATOR, Helper.TRANS_YANDEX);
                            editor.apply();
                            if (sharedpreferences.getString(Helper.SET_DEEPL_API_KEY, null) != null)
                                your_api_key.setText(sharedpreferences.getString(Helper.SET_DEEPL_API_KEY, ""));
                            break;
                        case 1:
                            your_api_key.setVisibility(View.VISIBLE);
                            editor.putInt(Helper.SET_TRANSLATOR, Helper.TRANS_DEEPL);
                            editor.apply();
                            if (sharedpreferences.getString(Helper.SET_YANDEX_API_KEY, null) != null)
                                your_api_key.setText(sharedpreferences.getString(Helper.SET_YANDEX_API_KEY, null));
                            break;
                        case 2:
                            your_api_key.setVisibility(View.GONE);
                            set_trans_forced.isChecked();
                            editor.putBoolean(Helper.SET_TRANS_FORCED, false);
                            editor.putInt(Helper.SET_TRANSLATOR, Helper.TRANS_NONE);
                            editor.apply();
                            break;
                    }
                    if( getActivity() != null)
                        getActivity().recreate();
                    Intent intent = new Intent(context, MainActivity.class);
                    intent.putExtra(Helper.INTENT_ACTION, Helper.BACK_TO_SETTINGS);
                    startActivity(intent);
                }
                count3++;
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //Resize
        final Spinner resize_layout_spinner = rootView.findViewById(R.id.set_resize_picture);
        ArrayAdapter<CharSequence> adapterResize = ArrayAdapter.createFromResource(getContext(),
                R.array.settings_resize_picture, android.R.layout.simple_spinner_item);
        resize_layout_spinner.setAdapter(adapterResize);
        int positionSpinnerResize = sharedpreferences.getInt(Helper.SET_PICTURE_RESIZE, Helper.S_NO);
        resize_layout_spinner.setSelection(positionSpinnerResize);
        resize_layout_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if( count4 > 0) {
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.putInt(Helper.SET_PICTURE_RESIZE, position);
                    editor.apply();
                }
                count4++;
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });












        boolean notify = sharedpreferences.getBoolean(Helper.SET_NOTIFY, true);
        final SwitchCompat switchCompatNotify = rootView.findViewById(R.id.set_notify);
        switchCompatNotify.setChecked(notify);
        final LinearLayout notification_settings = rootView.findViewById(R.id.notification_settings);
        if( notify)
            notification_settings.setVisibility(View.VISIBLE);
        else
            notification_settings.setVisibility(View.GONE);
        switchCompatNotify.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Save the state here
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_NOTIFY, isChecked);
                editor.apply();
                if( isChecked)
                    notification_settings.setVisibility(View.VISIBLE);
                else
                    notification_settings.setVisibility(View.GONE);
            }
        });



        boolean notif_follow = sharedpreferences.getBoolean(Helper.SET_NOTIF_FOLLOW, true);
        boolean notif_add = sharedpreferences.getBoolean(Helper.SET_NOTIF_ADD, true);
        boolean notif_mention = sharedpreferences.getBoolean(Helper.SET_NOTIF_MENTION, true);
        boolean notif_share = sharedpreferences.getBoolean(Helper.SET_NOTIF_SHARE, true);
        boolean notif_poll = sharedpreferences.getBoolean(Helper.SET_NOTIF_POLL, true);

        boolean notif_wifi = sharedpreferences.getBoolean(Helper.SET_WIFI_ONLY, false);
        boolean notif_silent = sharedpreferences.getBoolean(Helper.SET_NOTIF_SILENT, false);


        final String time_from = sharedpreferences.getString(Helper.SET_TIME_FROM, "07:00");
        final String time_to = sharedpreferences.getString(Helper.SET_TIME_TO, "22:00");


        final CheckBox set_notif_follow = rootView.findViewById(R.id.set_notif_follow);
        final CheckBox set_notif_follow_add = rootView.findViewById(R.id.set_notif_follow_add);
        final CheckBox set_notif_follow_mention = rootView.findViewById(R.id.set_notif_follow_mention);
        final CheckBox set_notif_follow_share = rootView.findViewById(R.id.set_notif_follow_share);
        final CheckBox set_notif_follow_poll = rootView.findViewById(R.id.set_notif_follow_poll);


        final SwitchCompat switchCompatWIFI = rootView.findViewById(R.id.set_wifi_only);
        final SwitchCompat switchCompatSilent = rootView.findViewById(R.id.set_silence);

        final Button settings_time_from = rootView.findViewById(R.id.settings_time_from);
        final Button settings_time_to = rootView.findViewById(R.id.settings_time_to);

        final LinearLayout channels_container = rootView.findViewById(R.id.channels_container);
        final Button sound_boost = rootView.findViewById(R.id.sound_boost);
        final Button sound_fav = rootView.findViewById(R.id.sound_fav);
        final Button sound_follow = rootView.findViewById(R.id.sound_follow);
        final Button sound_mention = rootView.findViewById(R.id.sound_mention);
        final Button sound_poll = rootView.findViewById(R.id.sound_poll);
        final Button sound_backup = rootView.findViewById(R.id.sound_backup);
        final Button sound_media = rootView.findViewById(R.id.sound_media);
        Button set_notif_sound = rootView.findViewById(R.id.set_notif_sound);
        settings_time_from.setText(time_from);
        settings_time_to.setText(time_to);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            set_notif_sound.setVisibility(View.GONE);
            channels_container.setVisibility(View.VISIBLE);

            sound_boost.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS);
                    intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName());
                    intent.putExtra(Settings.EXTRA_CHANNEL_ID, "channel_boost");
                    startActivity(intent);
                }
            });

            sound_fav.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS);
                    intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName());
                    intent.putExtra(Settings.EXTRA_CHANNEL_ID, "channel_fav");
                    startActivity(intent);
                }
            });

            sound_follow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS);
                    intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName());
                    intent.putExtra(Settings.EXTRA_CHANNEL_ID, "channel_follow");
                    startActivity(intent);
                }
            });

            sound_mention.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS);
                    intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName());
                    intent.putExtra(Settings.EXTRA_CHANNEL_ID, "channel_mention");
                    startActivity(intent);
                }
            });

            sound_poll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS);
                    intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName());
                    intent.putExtra(Settings.EXTRA_CHANNEL_ID, "channel_poll");
                    startActivity(intent);
                }
            });

            sound_backup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS);
                    intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName());
                    intent.putExtra(Settings.EXTRA_CHANNEL_ID, "channel_backup");
                    startActivity(intent);
                }
            });

            sound_media.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS);
                    intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName());
                    intent.putExtra(Settings.EXTRA_CHANNEL_ID, "channel_store");
                    startActivity(intent);
                }
            });
        }else{
            set_notif_sound.setVisibility(View.VISIBLE);
            channels_container.setVisibility(View.GONE);
            set_notif_sound.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, context.getString(R.string.select_sound));
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, (Uri) null);
                    startActivityForResult(intent, ACTIVITY_CHOOSE_SOUND);
                }
            });
        }


        boolean enable_time_slot = sharedpreferences.getBoolean(Helper.SET_ENABLE_TIME_SLOT, true);
        final CheckBox set_enable_time_slot = rootView.findViewById(R.id.set_enable_time_slot);
        set_enable_time_slot.setChecked(enable_time_slot);

        set_enable_time_slot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_ENABLE_TIME_SLOT, set_enable_time_slot.isChecked());
                editor.apply();
            }
        });


        settings_time_from.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] datetime = time_from.split(":");
                TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), style, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        SharedPreferences.Editor editor = sharedpreferences.edit();
                        String hours = (String.valueOf(hourOfDay).length() == 1) ? "0"+String.valueOf(hourOfDay):String.valueOf(hourOfDay);
                        String minutes = (String.valueOf(minute).length() == 1) ? "0"+String.valueOf(minute):String.valueOf(minute);
                        String newDate = hours + ":" + minutes;
                        if( Helper.compareDate(context, newDate, false) ) {
                            editor.putString(Helper.SET_TIME_FROM, newDate);
                            editor.apply();
                            settings_time_from.setText(newDate);
                        }else {
                            String ateRef = sharedpreferences.getString(Helper.SET_TIME_TO, "22:00");
                            Toasty.error(context, context.getString(R.string.settings_time_lower, ateRef), Toast.LENGTH_LONG).show();
                        }
                    }
                }, Integer.valueOf(datetime[0]), Integer.valueOf(datetime[1]), true);
                timePickerDialog.setTitle(context.getString(R.string.settings_hour_init));
                timePickerDialog.show();
            }
        });

        settings_time_to.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] datetime = time_to.split(":");
                TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(),style, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        SharedPreferences.Editor editor = sharedpreferences.edit();
                        String hours = (String.valueOf(hourOfDay).length() == 1) ? "0" + String.valueOf(hourOfDay) : String.valueOf(hourOfDay);
                        String minutes = (String.valueOf(minute).length() == 1) ? "0" + String.valueOf(minute) : String.valueOf(minute);
                        String newDate = hours + ":" + minutes;
                        if (Helper.compareDate(context, newDate, true)) {
                            editor.putString(Helper.SET_TIME_TO, newDate);
                            editor.apply();
                            settings_time_to.setText(newDate);
                        } else {
                            String ateRef = sharedpreferences.getString(Helper.SET_TIME_FROM, "07:00");
                            Toasty.error(context, context.getString(R.string.settings_time_greater, ateRef), Toast.LENGTH_LONG).show();
                        }
                    }
                }, Integer.valueOf(datetime[0]), Integer.valueOf(datetime[1]), true);
                timePickerDialog.setTitle(context.getString(R.string.settings_hour_end));
                timePickerDialog.show();
            }
        });


        final Spinner action_notification = rootView.findViewById(R.id.action_notification);
        ArrayAdapter<CharSequence> adapterAction = ArrayAdapter.createFromResource(getContext(),
                R.array.action_notification, android.R.layout.simple_spinner_item);
        action_notification.setAdapter(adapterAction);
        int positionNotificationAntion;
        switch (sharedpreferences.getInt(Helper.SET_NOTIFICATION_ACTION, Helper.ACTION_ACTIVE)){
            case Helper.ACTION_ACTIVE:
                positionNotificationAntion = 0;
                break;
            case Helper.ACTION_SILENT:
                positionNotificationAntion = 1;
                break;
            default:
                positionNotificationAntion = 0;
        }
        action_notification.setSelection(positionNotificationAntion);
        action_notification.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if( count7 > 0 ) {
                    SharedPreferences.Editor editor = sharedpreferences.edit();

                    switch (position) {
                        case 0:
                            editor.putInt(Helper.SET_NOTIFICATION_ACTION, Helper.ACTION_ACTIVE);
                            editor.apply();
                            break;
                        case 1:
                            editor.putInt(Helper.SET_NOTIFICATION_ACTION, Helper.ACTION_SILENT);
                            editor.apply();
                            break;
                    }
                }
                count7++;
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        set_notif_follow.setChecked(notif_follow);
        set_notif_follow_add.setChecked(notif_add);
        set_notif_follow_mention.setChecked(notif_mention);
        set_notif_follow_share.setChecked(notif_share);
        set_notif_follow_poll.setChecked(notif_poll);


        switchCompatWIFI.setChecked(notif_wifi);
        switchCompatSilent.setChecked(notif_silent);


        set_notif_follow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_NOTIF_FOLLOW, set_notif_follow.isChecked());
                editor.apply();
            }
        });
        set_notif_follow_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_NOTIF_ADD, set_notif_follow_add.isChecked());
                editor.apply();
            }
        });
        set_notif_follow_mention.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_NOTIF_MENTION, set_notif_follow_mention.isChecked());
                editor.apply();
            }
        });
        set_notif_follow_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_NOTIF_SHARE, set_notif_follow_share.isChecked());
                editor.apply();
            }
        });
        set_notif_follow_poll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_NOTIF_POLL, set_notif_follow_poll.isChecked());
                editor.apply();
            }
        });


        switchCompatWIFI.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Save the state here
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_WIFI_ONLY, isChecked);
                editor.apply();

            }
        });

        final Spinner led_colour_spinner = rootView.findViewById(R.id.led_colour_spinner);
        final TextView ledLabel = rootView.findViewById(R.id.set_led_colour_label);

        switchCompatSilent.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Save the state here
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_NOTIF_SILENT, isChecked);
                editor.apply();

                if (isChecked) {
                    ledLabel.setEnabled(true);
                    led_colour_spinner.setEnabled(true);
                } else {
                    ledLabel.setEnabled(false);
                    for (View lol : led_colour_spinner.getTouchables()) {
                        lol.setEnabled(false);
                    }
                }
            }
        });

        if (sharedpreferences.getBoolean(Helper.SET_NOTIF_SILENT, false)) {

            ledLabel.setEnabled(true);
            led_colour_spinner.setEnabled(true);

            ArrayAdapter<CharSequence> adapterLEDColour = ArrayAdapter.createFromResource(getContext(), R.array.led_colours, android.R.layout.simple_spinner_item);
            led_colour_spinner.setAdapter(adapterLEDColour);
            int positionSpinnerLEDColour = (sharedpreferences.getInt(Helper.SET_LED_COLOUR, Helper.LED_COLOUR));
            led_colour_spinner.setSelection(positionSpinnerLEDColour);

            led_colour_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (count6 > 0) {
                        SharedPreferences.Editor editor = sharedpreferences.edit();
                        editor.putInt(Helper.SET_LED_COLOUR, position);
                        editor.apply();
                    } else {
                        count6++;
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        }

        else {
            ledLabel.setEnabled(false);
            for (View lol : led_colour_spinner.getTouchables()) {
                lol.setEnabled(false);
            }
        }
        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void takeScreenShot() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                Bitmap bitmap = Bitmap.createBitmap(containerView.getWidth(),
                        containerView.getHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                containerView.draw(canvas);
                ContentSettingsFragment.this.bitmap = bitmap;
            }
        };

        thread.start();
    }

    @Override
    public Bitmap getBitmap() {
        return bitmap;
    }


    //From: https://gist.github.com/asifmujteba/d89ba9074bc941de1eaa#file-asfurihelper
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static String getPath(final Context context, final Uri uri) {
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };
                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }
}
