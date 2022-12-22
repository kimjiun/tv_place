package com.mobitant.yesterdaytv;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.mobitant.yesterdaytv.item.MemberInfoItem;
import com.mobitant.yesterdaytv.lib.EtcLib;
import com.mobitant.yesterdaytv.lib.MyLog;
import com.mobitant.yesterdaytv.lib.MyToast;
import com.mobitant.yesterdaytv.lib.StringLib;
import com.mobitant.yesterdaytv.remote.RemoteService;
import com.mobitant.yesterdaytv.remote.ServiceGenerator;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.GregorianCalendar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = this.getClass().getSimpleName();
    Context context;

    ImageView profileIconImage;
    ImageView profileIconChangeImage;
    EditText nameEdit;
    EditText sextypeEdit;
    EditText birthEdit;
    EditText phoneEdit;
    TextView toolbarTitle;
    MemberInfoItem currentItem;

    InputMethodManager imm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        context = this;

        currentItem = ((MyApp) getApplication()).getMemberInfoItem();

        setToolbar();
        setView();
    }

    @Override
    protected void onResume() {
        super.onResume();

        MyLog.d(TAG, RemoteService.MEMBER_ICON_URL + currentItem.memberIconFilename);

        if (StringLib.getInstance().isBlank(currentItem.memberIconFilename)) {
            Picasso.with(this).load(R.drawable.ic_person).into(profileIconImage);
        } else {
            Picasso.with(this)
                    .load(RemoteService.MEMBER_ICON_URL + currentItem.memberIconFilename)
                    .into(profileIconImage);
        }
    }

    private void setToolbar() {
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbarTitle = (TextView)findViewById(R.id.toolbar_title);
        final ActionBar actionBar = getSupportActionBar();


        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
            toolbarTitle.setText(R.string.profile_setting);
        }
    }

    private void setView() {
        imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);

        profileIconImage = (ImageView) findViewById(R.id.profile_icon);
        profileIconImage.setOnClickListener(this);

        profileIconChangeImage = (ImageView) findViewById(R.id.profile_icon_change);
        profileIconChangeImage.setOnClickListener(this);

        nameEdit = (EditText) findViewById(R.id.profile_name);
        nameEdit.setText(currentItem.name);

        sextypeEdit = (EditText) findViewById(R.id.profile_sextype);
        sextypeEdit.setText(currentItem.sextype);
        sextypeEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                setSexTypeDialog();
            }
        });

        birthEdit = (EditText) findViewById(R.id.profile_birth);
        birthEdit.setText(currentItem.birthday);
        birthEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                setBirthdayDialog();
            }
        });

        String phoneNumber = EtcLib.getInstance().getPhoneNumber(context);

        phoneEdit = (EditText) findViewById(R.id.profile_phone);
        phoneEdit.setText(phoneNumber);

        TextView phoneStateEdit = (TextView) findViewById(R.id.phone_state);
        if (phoneNumber.startsWith("0")) {
            phoneStateEdit.setText("(" + getResources().getString(R.string.device_number) + ")");
        } else {
            phoneStateEdit.setText("(" + getResources().getString(R.string.phone_number) + ")");
        }
    }

    private void setSexTypeDialog() {
        final String[] sexTypes = new String[2];
        sexTypes[0] = getResources().getString(R.string.sex_man);
        sexTypes[1] = getResources().getString(R.string.sex_woman);

        new AlertDialog.Builder(this)
                .setItems(sexTypes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which >= 0) {
                            sextypeEdit.setText(sexTypes[which]);
                        }
                        dialog.dismiss();
                    }
                }).show();
    }

    private void setBirthdayDialog() {
        GregorianCalendar calendar = new GregorianCalendar();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                String myMonth;
                if (monthOfYear + 1 < 10) {
                    myMonth = "0" + (monthOfYear + 1);
                } else {
                    myMonth = "" + (monthOfYear + 1);
                }

                String myDay;
                if (dayOfMonth < 10) {
                    myDay = "0" + dayOfMonth;
                } else {
                    myDay = "" + dayOfMonth;
                }

                String date = year + " " + myMonth + " " + myDay;
                birthEdit.setText(date);
            }
        }, year, month, day).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_submit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                close();
                break;

            case R.id.action_submit:
                save();
                break;
        }

        return true;
    }

    private MemberInfoItem getMemberInfoItem() {
        MemberInfoItem item = new MemberInfoItem();
        item.phone = EtcLib.getInstance().getPhoneNumber(context);
        item.name = nameEdit.getText().toString();
        item.sextype = sextypeEdit.getText().toString();
        item.birthday = birthEdit.getText().toString().replace(" ", "");

        return item;
    }

    private boolean isChanged(MemberInfoItem newItem) {
        if (newItem.name.trim().equals(currentItem.name)
                && newItem.sextype.trim().equals(currentItem.sextype)
                && newItem.birthday.trim().equals(currentItem.birthday)) {
            Log.d(TAG, "return " + false);
            return false;
        } else {
            return true;
        }
    }

    private boolean isNoName(MemberInfoItem newItem) {
        if (StringLib.getInstance().isBlank(newItem.name)) {
            return true;
        } else {
            return false;
        }
    }

    private void close() {
        MemberInfoItem newItem = getMemberInfoItem();

        if (!isChanged(newItem) && !isNoName(newItem)) {
            finish();
        } else if (isNoName(newItem)) {
            MyToast.s(context, R.string.name_need);
            finish();
        } else {
            new AlertDialog.Builder(this).setTitle(R.string.change_save)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            save();
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .show();
        }
    }

    private void save() {
        final MemberInfoItem newItem = getMemberInfoItem();

        if (!isChanged(newItem)) {
            MyToast.s(this, R.string.no_change);
            finish();
            return;
        }
        MyLog.d(TAG, "insertItem " + newItem.toString());
        RemoteService remoteService =
                ServiceGenerator.createService(RemoteService.class);

        Call<String> call = remoteService.insertMemberInfo(newItem);
        MyLog.d(TAG, "here seq : " +newItem.seq);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    String seq = response.body();
                    try {
                        currentItem.seq = Integer.parseInt(seq);
                        if (currentItem.seq == 0) {
                            MyToast.s(context, R.string.member_insert_fail_message);
                            return;
                        }
                    } catch (Exception e) {
                        MyToast.s(context, R.string.member_insert_fail_message);
                        return;
                    }
                    currentItem.name = newItem.name;
                    currentItem.sextype = newItem.sextype;
                    currentItem.birthday = newItem.birthday;
                    finish();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
            }
        });
    }

    @Override
    public void onBackPressed() {
        close();
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.profile_icon || v.getId() == R.id.profile_icon_change) {
            startProfileIconChange();
        }
    }

    private void startProfileIconChange() {
        Intent intent = new Intent(this, ProfileIconActivity.class);
        startActivity(intent);
    }
}