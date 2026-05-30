/*******************************************************************************
 * Copyright (c) 2014 Michael Hölzl <mihoelzl@gmail.com>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Michael Hölzl <mihoelzl@gmail.com> - initial implementation
 *     Thomas Sigmund - data base, key set, channel set selection and GET DATA integration
 ******************************************************************************/
package at.fhooe.usmile.gpjshell;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class SetInstallParamActivity extends Activity {
        private EditText mEditParams = null;
        private EditText mEditPrivileges = null;
        private Button mSetBtn = null;
        private Button mCancelBtn = null;

        // Privilege checkboxes
        private CheckBox mCheckDefaultSelected = null;
        private CheckBox mCheckSecurityDomain = null;
        private CheckBox mCheckDapVerification = null;
        private CheckBox mCheckGlobalRegistry = null;
        private CheckBox mCheckTokenVerification = null;
        private CheckBox mCheckReceiptGeneration = null;
        private CheckBox mCheckCipheredDap = null;
        private CheckBox mCheckAuthorizedMgmt = null;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_set_install_param);

                mEditParams = (EditText) findViewById(R.id.edit_parameter_parameters);
                mEditPrivileges = (EditText) findViewById(R.id.edit_parameter_privileges);
                mSetBtn = (Button) findViewById(R.id.btn_parameter_set);
                mCancelBtn = (Button) findViewById(R.id.btn_parameter_cancel);

                // Privilege checkboxes
                mCheckDefaultSelected = (CheckBox) findViewById(R.id.check_default_selected);
                mCheckSecurityDomain = (CheckBox) findViewById(R.id.check_security_domain);
                mCheckDapVerification = (CheckBox) findViewById(R.id.check_dap_verification);
                mCheckGlobalRegistry = (CheckBox) findViewById(R.id.check_global_registry);
                mCheckTokenVerification = (CheckBox) findViewById(R.id.check_token_verification);
                mCheckReceiptGeneration = (CheckBox) findViewById(R.id.check_receipt_generation);
                mCheckCipheredDap = (CheckBox) findViewById(R.id.check_ciphered_dap);
                mCheckAuthorizedMgmt = (CheckBox) findViewById(R.id.check_authorized_management);

                // Pre-fill from previous values if available
                Bundle extras = getIntent().getExtras();
                if (extras != null) {
                        byte[] prevParams = extras.getByteArray("params");
                        if (prevParams != null && prevParams.length > 0) {
                                mEditParams.setText(GPUtils.byteArrayToString(prevParams));
                        }
                        byte prevPriv = extras.getByte("privileges", (byte) 0);
                        int prevPrivInt = prevPriv & 0xFF;
                        if (prevPrivInt > 0) {
                                mEditPrivileges.setText(String.valueOf(prevPrivInt));
                                // Set checkboxes based on bits
                                mCheckSecurityDomain.setChecked((prevPrivInt & 0x01) != 0);
                                mCheckDapVerification.setChecked((prevPrivInt & 0x02) != 0);
                                mCheckDefaultSelected.setChecked((prevPrivInt & 0x04) != 0);
                                mCheckGlobalRegistry.setChecked((prevPrivInt & 0x08) != 0);
                                mCheckTokenVerification.setChecked((prevPrivInt & 0x10) != 0);
                                mCheckReceiptGeneration.setChecked((prevPrivInt & 0x20) != 0);
                                mCheckCipheredDap.setChecked((prevPrivInt & 0x40) != 0);
                                mCheckAuthorizedMgmt.setChecked((prevPrivInt & 0x80) != 0);
                        }
                }

                mSetBtn.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                                Intent intent = new Intent();

                                // Calculate privileges from checkboxes
                                int privileges = 0;
                                if (mCheckDefaultSelected.isChecked())  privileges |= 0x04;
                                if (mCheckSecurityDomain.isChecked())   privileges |= 0x01;
                                if (mCheckDapVerification.isChecked())  privileges |= 0x02;
                                if (mCheckGlobalRegistry.isChecked())   privileges |= 0x08;
                                if (mCheckTokenVerification.isChecked())privileges |= 0x10;
                                if (mCheckReceiptGeneration.isChecked())privileges |= 0x20;
                                if (mCheckCipheredDap.isChecked())      privileges |= 0x40;
                                if (mCheckAuthorizedMgmt.isChecked())   privileges |= 0x80;

                                // Custom privileges value overrides checkboxes
                                if (mEditPrivileges.getText() != null
                                                && mEditPrivileges.getText().length() > 0) {
                                        try {
                                                int customPriv = Integer.parseInt(mEditPrivileges.getText().toString());
                                                if (customPriv < 0 || customPriv > 255) {
                                                        Toast.makeText(SetInstallParamActivity.this,
                                                                        "Privileges must be 0-255",
                                                                        Toast.LENGTH_LONG).show();
                                                        return;
                                                }
                                                privileges = customPriv;
                                        } catch (NumberFormatException e) {
                                                Toast.makeText(SetInstallParamActivity.this,
                                                                "Invalid privileges value",
                                                                Toast.LENGTH_LONG).show();
                                                return;
                                        }
                                }

                                intent.putExtra("privileges", (byte) privileges);

                                // Parse install parameters (hex string -> byte[])
                                if (mEditParams.getText() != null
                                                && mEditParams.getText().length() > 0) {
                                        try {
                                                byte[] params = GPUtils.convertHexStringToByteArray(
                                                                mEditParams.getText().toString());
                                                intent.putExtra("params", params);
                                        } catch (Exception e) {
                                                Toast.makeText(SetInstallParamActivity.this,
                                                                "Invalid hex parameters",
                                                                Toast.LENGTH_LONG).show();
                                                return;
                                        }
                                }

                                setResult(RESULT_OK, intent);
                                finish();
                        }
                });

                mCancelBtn = (Button) findViewById(R.id.btn_parameter_cancel);
                mCancelBtn.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                                setResult(RESULT_CANCELED);
                                finish();
                        }
                });
        }

        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
                getMenuInflater().inflate(R.menu.set_install_param, menu);
                return true;
        }
}
