package co.circe.respos.fragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import org.json.JSONObject;

import co.circe.respos.R;
import co.circe.respos.RewardsActivity;
import co.circe.respos.font.RobotoTextView;
import co.circe.respos.library.NotifyingScrollView;
import co.circe.respos.library.UserFunctions;
import co.circe.respos.util.basic_utils;
import co.circe.respos.view.FloatLabeledEditText;
import co.circe.respos.view.ScrollViewFragment;

/**
 * Created by akhil on 17/6/15.
 */
public class RedeemRechargeScrollViewFragment extends ScrollViewFragment {

    basic_utils bu;
    private FloatLabeledEditText number;
    private Spinner operator;
    private FloatLabeledEditText amount;
    private RobotoTextView submit;
    String amount_text;
    String number_text;
    String operator_text;


    public static final String TAG = RedeemRechargeScrollViewFragment.class.getSimpleName();

    public static Fragment newInstance(int position) {
        RedeemRechargeScrollViewFragment fragment = new RedeemRechargeScrollViewFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_POSITION, position);
        fragment.setArguments(args);
        return fragment;
    }

    public RedeemRechargeScrollViewFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mPosition = getArguments().getInt(ARG_POSITION);

        bu = new basic_utils(getActivity());
        View view = inflater.inflate(R.layout.fragment_redeem_recharge_scroll_view, container, false);
        Spinner spinner = (Spinner) view.findViewById(R.id.operator_spin);
        number = (FloatLabeledEditText) view.findViewById(R.id.number_edit);
        operator = (Spinner) view.findViewById(R.id.operator_spin);
        amount = (FloatLabeledEditText) view.findViewById(R.id.amount_edit);
        submit = (RobotoTextView) view.findViewById(R.id.submit_recharge);

        // Onclick
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                clearErrors();

                boolean cancel = false;
                View focusView = null;

                // Store values at the time of the login attempt.
                number_text = number.getText().toString();
                operator_text = operator.getSelectedItem().toString();
                amount_text = amount.getText().toString();

                // Check for a valid number.
                if (TextUtils.isEmpty(number_text)) {
                    number.setError("Field is empty");
                    focusView = number;
                    cancel = true;
                } else if (number_text.length() < 10) {
                    number.setError("Mobile number not valid");
                    focusView = number;
                    cancel = true;
                }

                // Change operator
                if(operator_text == "Airtel") {
                    operator_text = "AT";
                }
                else if(operator_text =="Aircel") {
                    operator_text = "AL";
                }
                else if(operator_text =="BSNL") {
                    operator_text = "BS";
                }
                else if(operator_text =="BSNL Special") {
                    operator_text = "BSS";
                }
                else if(operator_text =="Idea") {
                    operator_text = "IDX";
                }
                else if(operator_text =="Vodafone") {
                    operator_text = "VF";
                }
                else if(operator_text =="Docomo GSM") {
                    operator_text = "TD";
                }
                else if(operator_text =="Docomo GSM Special") {
                    operator_text = "TDS";
                }
                else if(operator_text =="Docomo CDMA (Indicom)") {
                    operator_text = "TI";
                }
                else if(operator_text =="Reliance GSM") {
                    operator_text = "RG";
                }
                else if(operator_text =="Reliance CDMA") {
                    operator_text = "RL";
                }
                else if(operator_text =="MTS") {
                    operator_text = "MS";
                }
                else if(operator_text =="Uninor") {
                    operator_text = "UN";
                }
                else if(operator_text =="Uninor Special") {
                    operator_text = "UNS";
                }
                else if(operator_text =="Videocon") {
                    operator_text = "VD";
                }
                else if(operator_text =="Videocon Special") {
                    operator_text = "VDS";
                }
                else if(operator_text =="MTNL Mumbai") {
                    operator_text = "MTM";
                }
                else if(operator_text =="MTNL Mumbai Special") {
                    operator_text = "MTMS";
                }
                else if(operator_text =="MTNL Delhi") {
                    operator_text = "MTD";
                }
                else if(operator_text =="MTNL Delhi Special") {
                    operator_text = "MTDS";
                }
                else if(operator_text =="Virgin GSM") {
                    operator_text = "VG";
                }
                else if(operator_text == "Virgin GSM Special") {
                    operator_text = "VGS";
                }
                else if(operator_text == "Virgin CDMA") {
                    operator_text = "VC";
                }
                else if(operator_text == "T24") {
                    operator_text = "T24";
                }
                else if(operator_text == "T24 Special") {
                    operator_text = "T24S";
                }

                // Checing amount
                if (TextUtils.isEmpty(amount_text)) {
                    amount.setError("Field is empty");
                    focusView = amount;
                    cancel = true;
                }
                if(Integer.valueOf(amount_text) < 10) {
                    amount.setError("Amount should be greater than 10");
                    focusView = amount;
                    cancel = true;
                }

                if (cancel) {
                    // There was an error; don't attempt login and focus the first
                    // form field with an error.
                    focusView.requestFocus();
                } else {
                    // Show a progress spinner, and kick off a background task to
                    // perform the user login attempt.
                    new process_recharge().execute();

                }

            }
        });

        spinner.getBackground().setColorFilter(getResources().getColor(R.color.material_yellow_400), PorterDuff.Mode.SRC_ATOP);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),R.array.operator_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        mScrollView = (NotifyingScrollView) view.findViewById(R.id.scrollview);
        setScrollViewOnScrollListener();
        return view;
    }

    private class process_recharge extends AsyncTask<String, String, JSONObject> {


        private ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(getActivity());
            pDialog.setTitle("Contacting Servers");
            pDialog.setMessage("Fueling your mobile ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected JSONObject doInBackground(String... args) {

            UserFunctions userFunction = new UserFunctions();
            JSONObject json = userFunction.recharge(bu.get_defaults("uid"),number_text, operator_text, amount_text);
            return json;
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            Log.d(TAG,json.toString());
            pDialog.dismiss();
            try {
                if (json.getString("success") != null) {
                    String res = json.getString("success");
                    if (Integer.parseInt(res) == 1) {
                        showAlertDialog(getActivity(), "Success", "Your mobile has been recharged !");
                    } else {
                        if (Integer.parseInt(json.getString("error")) == 1) {
                            showAlertDialog(getActivity(), "Error", "Insufficient balance. Please refresh.");
                        }
                        else if (Integer.parseInt(json.getString("error")) == 3)
                        {
                            // Start the service to get updated status

                            showAlertDialog(getActivity(), "Patience", "Your request is being processed. Pleas wait !");
                            Intent i = new Intent(getActivity(), RewardsActivity.class);
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(i);
                            getActivity().finish();

                        } else {
                            Log.d(TAG,"2");
                            showAlertDialog(getActivity(), "Error", json.getJSONObject("result").getString("error"));
                        }
                    }
                } else {
                    showAlertDialog(getActivity(), "Error", "Server Busy !");
                }
            } catch (Exception e) {
                Log.d(TAG, e.toString());
            }
        }
    }

    public void clearErrors() {
        number.setError(null);
        amount.setError(null);
    }

    @SuppressWarnings("deprecation")
    public void showAlertDialog(Context context, String title, String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setIcon(android.R.drawable.ic_dialog_alert);
        alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        alertDialog.show();
    }
}
