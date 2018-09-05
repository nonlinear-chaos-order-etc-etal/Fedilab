package fr.gouv.etalab.mastodon.fragments;
/* Copyright 2018 Thomas Schneider
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
 * You should have received a copy of the GNU General Public License along with Mastalab; if not,
 * see <http://www.gnu.org/licenses>. */

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import fr.gouv.etalab.mastodon.R;
import fr.gouv.etalab.mastodon.activities.MainActivity;
import fr.gouv.etalab.mastodon.asynctasks.ManageFiltersAsyncTask;
import fr.gouv.etalab.mastodon.client.APIResponse;
import fr.gouv.etalab.mastodon.client.Entities.Filters;
import fr.gouv.etalab.mastodon.drawers.FilterAdapter;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.interfaces.OnFilterActionInterface;


/**
 * Created by Thomas on 05/09/2018.
 * Fragment to display Filters
 */
public class DisplayFiltersFragment extends Fragment implements OnFilterActionInterface {


    private Context context;
    private AsyncTask<Void, Void, Void> asyncTask;
    private List<fr.gouv.etalab.mastodon.client.Entities.Filters> filters;
    private TextView no_action_text;
    private RelativeLayout mainLoader;
    private FloatingActionButton add_new;
    private FilterAdapter filterAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //View for fragment is the same that fragment accounts
        View rootView = inflater.inflate(R.layout.fragment_filters, container, false);

        context = getContext();
        filters = new ArrayList<>();


        ListView lv_filters = rootView.findViewById(R.id.lv_filters);
        RelativeLayout textviewNoAction = rootView.findViewById(R.id.no_action);
        no_action_text = rootView.findViewById(R.id.no_action_text);
        mainLoader = rootView.findViewById(R.id.loader);
        RelativeLayout nextElementLoader = rootView.findViewById(R.id.loading_next_items);
        mainLoader.setVisibility(View.VISIBLE);
        nextElementLoader.setVisibility(View.GONE);
        filterAdapter = new FilterAdapter(context, filters, textviewNoAction);
        lv_filters.setAdapter(filterAdapter);
        no_action_text.setVisibility(View.GONE);
        asyncTask = new ManageFiltersAsyncTask(context, ManageFiltersAsyncTask.action.GET_ALL_FILTER, null, DisplayFiltersFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        try {
            add_new = ((MainActivity) context).findViewById(R.id.add_new);
        }catch (Exception ignored){}
        if( add_new != null)
        add_new.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
                LayoutInflater inflater = getLayoutInflater();
                @SuppressLint("InflateParams") View dialogView = inflater.inflate(R.layout.add_filter, null);
                dialogBuilder.setView(dialogView);

                EditText add_phrase = dialogView.findViewById(R.id.add_phrase);
                CheckBox context_home = dialogView.findViewById(R.id.context_home);
                CheckBox context_public = dialogView.findViewById(R.id.context_public);
                CheckBox context_notification = dialogView.findViewById(R.id.context_notification);
                CheckBox context_conversation = dialogView.findViewById(R.id.context_conversation);
                CheckBox context_whole_word = dialogView.findViewById(R.id.context_whole_word);
                CheckBox context_drop = dialogView.findViewById(R.id.context_drop);

                dialogBuilder.setPositiveButton(R.string.validate, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                        if( add_phrase.getText() != null && add_phrase.getText().toString().trim().length() > 0 ) {
                            Filters filter = new Filters();
                            ArrayList<String> contextFilter = new ArrayList<>();
                            if( context_home.isChecked())
                                contextFilter.add("home");
                            if( context_public.isChecked())
                                contextFilter.add("public");
                            if( context_notification.isChecked())
                                contextFilter.add("notifications");
                            if( context_conversation.isChecked())
                                contextFilter.add("thread");
                            filter.setContext(contextFilter);
                            filter.setPhrase(add_phrase.getText().toString());
                            filter.setWhole_word(context_whole_word.isChecked());
                            filter.setIrreversible(context_drop.isChecked());
                            new ManageFiltersAsyncTask(context, ManageFiltersAsyncTask.action.CREATE_FILTER, filter, DisplayFiltersFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        }
                        dialog.dismiss();
                        add_new.setEnabled(false);
                    }
                });
                dialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });


                AlertDialog alertDialog = dialogBuilder.create();
                alertDialog.setTitle(getString(R.string.action_filter_create));
                alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        //Hide keyboard
                        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                        assert imm != null;
                        imm.hideSoftInputFromWindow(add_phrase.getWindowToken(), 0);
                    }
                });
                if( alertDialog.getWindow() != null )
                    alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                alertDialog.show();
            }
        });
        return rootView;
    }



    @Override
    public void onCreate(Bundle saveInstance)
    {
        super.onCreate(saveInstance);
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    public void onDestroy() {
        super.onDestroy();
        if(asyncTask != null && asyncTask.getStatus() == AsyncTask.Status.RUNNING)
            asyncTask.cancel(true);
    }




    @Override
    public void onActionDone(ManageFiltersAsyncTask.action actionType, APIResponse apiResponse, int statusCode) {
        mainLoader.setVisibility(View.GONE);
        add_new.setEnabled(true);
        if( apiResponse.getError() != null){
            final SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
            boolean show_error_messages = sharedpreferences.getBoolean(Helper.SET_SHOW_ERROR_MESSAGES, true);
            if( show_error_messages)
                Toast.makeText(context, apiResponse.getError().getError(),Toast.LENGTH_LONG).show();
            return;
        }
        if( actionType == ManageFiltersAsyncTask.action.GET_ALL_FILTER) {
            if (apiResponse.getFilters() != null && apiResponse.getFilters().size() > 0) {
                this.filters.addAll(apiResponse.getFilters());
                filterAdapter.notifyDataSetChanged();

            } else {
                no_action_text.setVisibility(View.VISIBLE);
            }
        }else if( actionType == ManageFiltersAsyncTask.action.CREATE_FILTER){
            if (apiResponse.getFilters() != null && apiResponse.getFilters().size() > 0) {

                this.filters.add(0, apiResponse.getFilters().get(0));
                filterAdapter.notifyDataSetChanged();
            }else{
                Toast.makeText(context, apiResponse.getError().getError(),Toast.LENGTH_LONG).show();
            }
        }
    }
}
