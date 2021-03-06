package com.example.androidtheflash;

import static com.google.firebase.messaging.Constants.MessagePayloadKeys.SENDER_ID;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.androidtheflash.Model.DriverInfoModel;
import com.example.androidtheflash.Utils.UserUtils;
import com.firebase.ui.auth.AuthMethodPickerLayout;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;


import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;

public class SplashScreenActivity extends AppCompatActivity {
    
    private final static int LOGIN_REQUEST_CODE = 7171;
    private List<AuthUI.IdpConfig> providers;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener listener;

    @BindView(R.id.progress_bar)
    ProgressBar progress_bar;
    FirebaseDatabase database;
    DatabaseReference driverInfoRef;



    @Override
    protected void onStart(){
        super.onStart();
        delaySplashScreeen();
    }

    @Override
    protected void onStop(){
        if(firebaseAuth != null && listener != null)
            firebaseAuth.removeAuthStateListener(listener);
        super.onStop();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        init();
    }

    private void init(){

        ButterKnife.bind(this);

        database = FirebaseDatabase.getInstance();
        driverInfoRef = database.getReference(Common.DRIVER_INFO_REFERENCE);

        providers = Arrays.asList(
                new AuthUI.IdpConfig.PhoneBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build());
        firebaseAuth = FirebaseAuth.getInstance();
        listener = myFireBaseAuth -> {
            FirebaseUser user = myFireBaseAuth.getCurrentUser();
            if(user != null) {

                //update token
                FirebaseMessaging.getInstance().send(
                        new RemoteMessage.Builder(SENDER_ID + "fcm.googleapis.com")
                                .setMessageId("send")
                                .addData("key", "value")
                                .build());
                checkUserFromFirebase();
            }
            else
                showLoginLayout();
        };
    }



    private void checkUserFromFirebase() {
        driverInfoRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                        if(datasnapshot.exists())
                        {
                            //Toast.makeText(SplashScreenActivity.this,"User already register", Toast.LENGTH_SHORT).show();
                        DriverInfoModel driverInfoModel = datasnapshot.getValue(DriverInfoModel.class);
                        goToHomeAcitivy(driverInfoModel);
                        }
                        else
                        {
                            showRegisterLayout();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(SplashScreenActivity.this,""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void goToHomeAcitivy(DriverInfoModel driverInfoModel){
        Common.currentUser = driverInfoModel;
        startActivity(new Intent(SplashScreenActivity.this, DriverHomeActivity.class));
        finish();

    }
    private void showRegisterLayout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this,R.style.DialogTheme);
        View itemView = LayoutInflater.from(this).inflate(R.layout.layout_register,null);

        TextInputEditText edt_first_name = (TextInputEditText) itemView.findViewById(R.id.edit_first_name);
        TextInputEditText edt_last_name = (TextInputEditText) itemView.findViewById(R.id.edit_last_name);
        TextInputEditText edt_phone = (TextInputEditText) itemView.findViewById(R.id.edit_phone_number);

        Button btn_continue = (Button)itemView.findViewById(R.id.btn_register);

        //set data
        if(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber() != null &&
                !TextUtils.isEmpty(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber()))
            edt_phone.setText(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());

        //Set View
        builder.setView(itemView);
        AlertDialog dialog = builder.create();
        dialog.show();

        btn_continue.setOnClickListener(view -> {
            if(TextUtils.isEmpty(edt_first_name.getText().toString()))
            {
                Toast.makeText(this,"Vui l??ng nh???p t??n", Toast.LENGTH_SHORT).show();
                return;
            }
            else if(TextUtils.isEmpty(edt_last_name.getText().toString()))
            {
                Toast.makeText(this,"Vui l??ng nh???p h???", Toast.LENGTH_SHORT).show();
                return;
            }
            else if(TextUtils.isEmpty(edt_phone.getText().toString()))
            {
                Toast.makeText(this,"Vui l??ng nh???p s??? ??i???n tho???i", Toast.LENGTH_SHORT).show();
                return;
            }
            else
            {
                DriverInfoModel model = new DriverInfoModel();
                model.setFirstName(edt_first_name.getText().toString());
                model.setLastName(edt_last_name.getText().toString());
                model.setPhoneNumber(edt_phone.getText().toString());
                model.setRating(0.0);

                driverInfoRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .setValue(model)
                        .addOnFailureListener(e ->
                        {
                            dialog.dismiss();
                            Toast.makeText(SplashScreenActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        })
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this,"Register Succesfully!",Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        goToHomeAcitivy(model);
                        });
            }

        });
    }

    @SuppressWarnings("deprecation")
    private void showLoginLayout() {
        AuthMethodPickerLayout authMethodPickerLayout = new AuthMethodPickerLayout
                .Builder(R.layout.layout_sign_in)
                .setPhoneButtonId(R.id.btn_phone_sign_in)
                .setGoogleButtonId(R.id.btn_google_sign_in)
                .build();

        startActivityForResult(AuthUI.getInstance()
                .createSignInIntentBuilder()
               .setAuthMethodPickerLayout(authMethodPickerLayout)
                .setIsSmartLockEnabled(false)
                .setTheme(R.style.LoginTheme)
                .setAvailableProviders(providers)
                .build(),LOGIN_REQUEST_CODE);
    }

    private void delaySplashScreeen() {

        progress_bar.setVisibility(View.VISIBLE);

        Completable.timer(3, TimeUnit.SECONDS,
                AndroidSchedulers.mainThread())
                .subscribe(()-> firebaseAuth.addAuthStateListener(listener)

                        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == LOGIN_REQUEST_CODE)
        {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if(resultCode == RESULT_OK)
            {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            }
            else
            {
                Toast.makeText(this,"[ERROR]: " +response.getError().getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}