package app.fedilab.android.drawers;
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


import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import app.fedilab.android.R;
import app.fedilab.android.activities.MainActivity;
import app.fedilab.android.activities.ShowAccountActivity;
import app.fedilab.android.asynctasks.PostActionAsyncTask;
import app.fedilab.android.asynctasks.UpdateAccountInfoAsyncTask;
import app.fedilab.android.client.API;
import app.fedilab.android.client.Entities.Account;
import app.fedilab.android.client.Entities.Error;
import app.fedilab.android.helper.Helper;
import app.fedilab.android.interfaces.OnPostActionInterface;
import es.dmoral.toasty.Toasty;


/**
 * Created by Thomas on 07/05/2017.
 * Adapter for accounts asking a follow request
 */
public class AccountsFollowRequestAdapter extends RecyclerView.Adapter implements OnPostActionInterface {

    private List<Account> accounts;
    private Context context;
    private AccountsFollowRequestAdapter accountsFollowRequestAdapter;

    public AccountsFollowRequestAdapter(List<Account> accounts) {
        this.accounts = accounts;
        accountsFollowRequestAdapter = this;
    }


    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        return new AccountsFollowRequestAdapter.ViewHolder(layoutInflater.inflate(R.layout.drawer_account_follow_request, parent, false));
    }

    @Override
    public void onBindViewHolder(@NotNull RecyclerView.ViewHolder viewHolder, int position) {

        final AccountsFollowRequestAdapter.ViewHolder holder = (AccountsFollowRequestAdapter.ViewHolder) viewHolder;
        final Account account = accounts.get(position);
        holder.btn_authorize.getBackground().setColorFilter(ContextCompat.getColor(context, R.color.green_1), PorterDuff.Mode.MULTIPLY);
        holder.btn_reject.getBackground().setColorFilter(ContextCompat.getColor(context, R.color.red_1), PorterDuff.Mode.MULTIPLY);
        //TODO: check if Friendica has a way to accept/deny follow requests
        if(MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.GNU) {
            holder.btn_authorize.setVisibility(View.GONE);
            holder.btn_reject.setVisibility(View.GONE);
        }
        holder.account_dn.setText(Helper.shortnameToUnicode(account.getDisplay_name(), true));
        holder.account_un.setText(account.getAcct());
        //Profile picture
        Glide.with(holder.account_pp.getContext())
                .load(account.getAvatar())
                .into(holder.account_pp);
        holder.account_pp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAccountDetails(account);
            }
        });
        holder.account_dn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAccountDetails(account);
            }
        });
        holder.account_un.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAccountDetails(account);
            }
        });
        holder.btn_authorize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new PostActionAsyncTask(context, API.StatusAction.AUTHORIZE, account.getId(), AccountsFollowRequestAdapter.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });
        holder.btn_reject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new PostActionAsyncTask(context, API.StatusAction.REJECT, account.getId(), AccountsFollowRequestAdapter.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return accounts.size();
    }

    private void openAccountDetails(Account account) {
        Intent intent = new Intent(context, ShowAccountActivity.class);
        Bundle b = new Bundle();
        b.putParcelable("account", account);
        intent.putExtras(b);
        context.startActivity(intent);
    }

    @Override
    public void onPostAction(int statusCode, API.StatusAction statusAction, String userId, Error error) {
        if (error != null) {
            Toasty.error(context, error.getError(), Toast.LENGTH_LONG).show();
            return;
        }
        Helper.manageMessageStatusCode(context, statusCode, statusAction);
        //When authorizing or rejecting an account, this account is removed from the list
        List<Account> accountToRemove = new ArrayList<>();
        if (statusAction == API.StatusAction.AUTHORIZE || statusAction == API.StatusAction.REJECT) {
            for (Account account : accounts) {
                if (account.getId().equals(userId))
                    accountToRemove.add(account);
            }
            accounts.removeAll(accountToRemove);
            accountsFollowRequestAdapter.notifyDataSetChanged();
        }
    }


    private class ViewHolder extends RecyclerView.ViewHolder {
        ImageView account_pp;
        Button btn_authorize;
        Button btn_reject;
        TextView account_dn;
        TextView account_un;

        ViewHolder(View itemView) {
            super(itemView);
            account_pp = itemView.findViewById(R.id.account_pp);
            account_dn = itemView.findViewById(R.id.account_dn);
            account_un = itemView.findViewById(R.id.account_un);
            btn_authorize = itemView.findViewById(R.id.btn_authorize);
            btn_reject = itemView.findViewById(R.id.btn_reject);
        }
    }


}