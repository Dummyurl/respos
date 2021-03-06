/**
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package co.circe.respos.services;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import org.json.JSONObject;

import co.circe.respos.QuickstartPreferences;
import co.circe.respos.R;
import co.circe.respos.util.basic_utils;
import co.circe.respos.library.UserFunctions;

public class registration_intent_service extends IntentService {

    private static final String TAG = "RegIntentService";
    private static final String[] TOPICS = {"global"};
    private JSONObject json;

    // Basic utils object
    basic_utils bu;

    // User functions object
    UserFunctions uf = new UserFunctions();

    public registration_intent_service() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        // Basic utils object

        bu = new basic_utils(getApplicationContext());
        try {
            synchronized (TAG) {
                InstanceID instanceID = InstanceID.getInstance(this);
                String token = instanceID.getToken(getString(R.string.sender_id),GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
                Log.d(TAG, "GCM Registration Token: " + token);
                bu.set_defaults("gcm_token", token);
            }
        } catch (Exception e) {
                bu.set_defaults("gcm_sent_to_server","false");
        }
        Intent registrationComplete = new Intent(QuickstartPreferences.REGISTRATION_COMPLETE);
        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }

}
