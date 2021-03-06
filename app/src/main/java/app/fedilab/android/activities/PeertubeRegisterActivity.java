/* Copyright 2017 Thomas Schneider
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
package app.fedilab.android.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import app.fedilab.android.R;
import app.fedilab.android.asynctasks.CreateMastodonAccountAsyncTask;
import app.fedilab.android.asynctasks.RetrieveInstanceRegAsyncTask;
import app.fedilab.android.client.APIResponse;
import app.fedilab.android.client.Entities.AccountCreation;
import app.fedilab.android.client.Entities.InstanceReg;
import app.fedilab.android.drawers.InstancePeertubeRegAdapter;
import app.fedilab.android.helper.Helper;
import app.fedilab.android.interfaces.OnPostStatusActionInterface;
import app.fedilab.android.interfaces.OnRetrieveInstanceInterface;
import es.dmoral.toasty.Toasty;

import static android.os.AsyncTask.THREAD_POOL_EXECUTOR;

/**
 * Created by Thomas on 04/11/2019.
 * Register activity class
 */

public class PeertubeRegisterActivity extends BaseActivity implements OnRetrieveInstanceInterface, OnPostStatusActionInterface {


    private Button signup;
    private String instance;
    private TextView error_message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, MODE_PRIVATE);
        int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        switch (theme) {
            case Helper.THEME_LIGHT:
                setTheme(R.style.AppTheme_Fedilab);
                break;
            case Helper.THEME_DARK:
                setTheme(R.style.AppThemeDark);
                break;
            case Helper.THEME_BLACK:
                setTheme(R.style.AppThemeBlack);
                break;
            default:
                setTheme(R.style.AppThemeDark);
        }

        setContentView(R.layout.activity_register_peertube);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
            assert inflater != null;
            View view = inflater.inflate(R.layout.simple_bar, new LinearLayout(getApplicationContext()), false);
            view.setBackground(new ColorDrawable(ContextCompat.getColor(PeertubeRegisterActivity.this, R.color.cyanea_primary)));
            actionBar.setCustomView(view, new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            ImageView toolbar_close = actionBar.getCustomView().findViewById(R.id.toolbar_close);
            TextView toolbar_title = actionBar.getCustomView().findViewById(R.id.toolbar_title);
            toolbar_close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
            toolbar_title.setText(R.string.sign_up);
        }


        new RetrieveInstanceRegAsyncTask(PeertubeRegisterActivity.this, RetrieveInstanceRegAsyncTask.instanceType.PEERTUBE, null, PeertubeRegisterActivity.this).executeOnExecutor(THREAD_POOL_EXECUTOR);

        signup = findViewById(R.id.signup);
        EditText username = findViewById(R.id.username);
        EditText email = findViewById(R.id.email);
        EditText password = findViewById(R.id.password);
        EditText password_confirm = findViewById(R.id.password_confirm);
        CheckBox agreement = findViewById(R.id.agreement);
        error_message = findViewById(R.id.error_message);

        signup.setOnClickListener(view -> {
            error_message.setVisibility(View.GONE);
            if (username.getText().toString().trim().length() == 0 || email.getText().toString().trim().length() == 0 ||
                    password.getText().toString().trim().length() == 0 || password_confirm.getText().toString().trim().length() == 0 || !agreement.isChecked()) {
                Toasty.error(getApplicationContext(), getString(R.string.all_field_filled)).show();
                return;
            }
            if (!password.getText().toString().trim().equals(password_confirm.getText().toString().trim())) {
                Toasty.error(getApplicationContext(), getString(R.string.password_error)).show();
                return;
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email.getText().toString().trim()).matches()) {
                Toasty.error(getApplicationContext(), getString(R.string.email_error)).show();
                return;
            }
            if (password.getText().toString().trim().length() < 8) {
                Toasty.error(getApplicationContext(), getString(R.string.password_too_short)).show();
                return;
            }
            if (username.getText().toString().matches("[a-zA-Z0-9_]")) {
                Toasty.error(getApplicationContext(), getString(R.string.username_error)).show();
                return;
            }
            signup.setEnabled(false);
            AccountCreation accountCreation = new AccountCreation();
            accountCreation.setEmail(email.getText().toString().trim());
            accountCreation.setPassword(password.getText().toString().trim());
            accountCreation.setPasswordConfirm(password_confirm.getText().toString().trim());
            accountCreation.setUsername(username.getText().toString().trim());
            new CreateMastodonAccountAsyncTask(PeertubeRegisterActivity.this, RetrieveInstanceRegAsyncTask.instanceType.PEERTUBE, accountCreation, instance, PeertubeRegisterActivity.this).executeOnExecutor(THREAD_POOL_EXECUTOR);
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onRetrieveInstance(APIResponse apiResponse) {
        if (apiResponse.getError() != null) {
            Toasty.error(getApplicationContext(), getString(R.string.toast_error_instance_reg), Toast.LENGTH_LONG).show();
            return;
        }
        List<InstanceReg> instanceRegs = apiResponse.getInstanceRegs();
        RecyclerView lv_instances = findViewById(R.id.reg_category_view);
        InstancePeertubeRegAdapter instanceRegAdapter = new InstancePeertubeRegAdapter(PeertubeRegisterActivity.this, instanceRegs);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(PeertubeRegisterActivity.this);
        lv_instances.setLayoutManager(mLayoutManager);
        lv_instances.setNestedScrollingEnabled(false);
        lv_instances.setAdapter(instanceRegAdapter);


    }

    public void pickupInstance(String instance) {

        LinearLayout form_container = findViewById(R.id.form_container);
        LinearLayout drawer_layout = findViewById(R.id.drawer_layout);

        TextView host_reg = findViewById(R.id.host_reg);
        host_reg.setText(instance);
        this.instance = instance;

        drawer_layout.animate()
                .translationY(0)
                .alpha(0.0f)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        drawer_layout.setVisibility(View.GONE);
                        form_container.setVisibility(View.VISIBLE);
                    }
                });

        TextView change_instance = findViewById(R.id.change_instance);
        final SpannableString change = new SpannableString(String.format("(%s)", getString(R.string.change)));
        change.setSpan(new UnderlineSpan(), 0, change.length(), 0);
        change.setSpan(new ForegroundColorSpan(ContextCompat.getColor(PeertubeRegisterActivity.this, R.color.cyanea_accent_reference)), 0, change.length(),
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        change_instance.setText(change);
        change_instance.setOnClickListener(view -> {
            drawer_layout.setVisibility(View.VISIBLE);
            drawer_layout.animate()
                    .translationY(0)
                    .alpha(1.f)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            form_container.setVisibility(View.GONE);
                        }
                    });
        });

        TextView agreement_text = findViewById(R.id.agreement_text);

        TextView username_indicator = findViewById(R.id.username_indicator);
        username_indicator.setText(getString(R.string.username_indicator, instance));

        String tos = getString(R.string.tos);
        String content_agreement = getString(R.string.agreement_check_peertube,
                "<a href='https://" + instance + "/about/instance#terms-section' >" + tos + "</a>"
        );
        agreement_text.setMovementMethod(LinkMovementMethod.getInstance());
        agreement_text.setText(Html.fromHtml(content_agreement));
    }


    @Override
    public void onPostStatusAction(APIResponse apiResponse) {
        if (apiResponse.getError() != null) {
            String errorMessage;
            if (apiResponse.getError().getError() != null) {
                try {
                    String[] resp = apiResponse.getError().getError().split(":");
                    if (resp.length == 2)
                        errorMessage = apiResponse.getError().getError().split(":")[1];
                    else if (resp.length == 3)
                        errorMessage = apiResponse.getError().getError().split(":")[2];
                    else
                        errorMessage = getString(R.string.toast_error);
                } catch (Exception e) {
                    errorMessage = getString(R.string.toast_error);
                }
            } else {
                errorMessage = getString(R.string.toast_error);
            }
            error_message.setText(errorMessage);
            error_message.setVisibility(View.VISIBLE);
            signup.setEnabled(true);
            return;
        }
        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        int style;
        if (theme == Helper.THEME_DARK) {
            style = R.style.DialogDark;
        } else if (theme == Helper.THEME_BLACK) {
            style = R.style.DialogBlack;
        } else {
            style = R.style.Dialog;
        }
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(PeertubeRegisterActivity.this, style);
        dialogBuilder.setCancelable(false);
        dialogBuilder.setPositiveButton(R.string.validate, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });
        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.setTitle(getString(R.string.account_created));
        alertDialog.setMessage(getString(R.string.account_created_message, this.instance));
        alertDialog.show();
    }

}