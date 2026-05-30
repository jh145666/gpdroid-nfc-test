package at.fhooe.usmile.gpjshell;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import net.sourceforge.gpj.cardservices.AID;
import net.sourceforge.gpj.cardservices.AIDRegistryEntry;
import net.sourceforge.gpj.cardservices.GPUtil;
import net.sourceforge.gpj.cardservices.interfaces.GPTerminal;
import net.sourceforge.gpj.cardservices.interfaces.NfcTerminal;
import at.fhooe.usmile.gpjshell.MainActivity.APDU_COMMAND;
import at.fhooe.usmile.gpjshell.objects.GPChannelSet;
import at.fhooe.usmile.gpjshell.objects.GPKeyset;

public class DeleteAppletActivity extends Activity {

        private EditText mEditAid = null;
        private CheckBox mCheckDeleteDeps = null;
        private Button mBtnDelete = null;
        private Button mBtnCancel = null;
        private Button mBtnList = null;

        private GPKeyset mKeyset = null;
        private GPChannelSet mChannelSet = null;
        private int mSeekReader = 0;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_delete_applet);

                // Get keyset and channel set from intent extras
                Bundle extras = getIntent().getExtras();
                if (extras != null) {
                        mKeyset = (GPKeyset) extras.getSerializable(GPKeyset.KEYSET);
                        mChannelSet = (GPChannelSet) extras.getSerializable(GPChannelSet.CHANNEL_SET);
                        mSeekReader = extras.getInt("seekReader", 0);
                }

                mEditAid = (EditText) findViewById(R.id.edit_delete_aid);
                mCheckDeleteDeps = (CheckBox) findViewById(R.id.check_delete_deps);
                mBtnDelete = (Button) findViewById(R.id.btn_delete_confirm);
                mBtnCancel = (Button) findViewById(R.id.btn_delete_cancel);
                mBtnList = (Button) findViewById(R.id.btn_list_for_delete);

                mBtnDelete.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                                String aidHex = mEditAid.getText().toString().trim();
                                if (aidHex.length() == 0) {
                                        Toast.makeText(DeleteAppletActivity.this,
                                                        "Please enter an AID",
                                                        Toast.LENGTH_LONG).show();
                                        return;
                                }

                                // Validate hex
                                try {
                                        byte[] aidBytes = GPUtils.convertHexStringToByteArray(aidHex);
                                        if (aidBytes.length < 5 || aidBytes.length > 16) {
                                                Toast.makeText(DeleteAppletActivity.this,
                                                                "AID must be 5-16 bytes",
                                                                Toast.LENGTH_LONG).show();
                                                return;
                                        }
                                } catch (Exception e) {
                                        Toast.makeText(DeleteAppletActivity.this,
                                                        "Invalid hex AID",
                                                        Toast.LENGTH_LONG).show();
                                        return;
                                }

                                // Confirm dialog
                                new AlertDialog.Builder(DeleteAppletActivity.this)
                                        .setTitle("Confirm Delete")
                                        .setMessage("Are you sure you want to delete applet with AID: " + aidHex + "?")
                                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                        Intent resultIntent = new Intent();
                                                        resultIntent.putExtra("aid", aidHex);
                                                        resultIntent.putExtra("deleteDeps", mCheckDeleteDeps.isChecked());
                                                        setResult(RESULT_OK, resultIntent);
                                                        finish();
                                                }
                                        })
                                        .setNegativeButton("Cancel", null)
                                        .show();
                        }
                });

                mBtnCancel.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                                setResult(RESULT_CANCELED);
                                finish();
                        }
                });

                mBtnList.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                                // List applets on card, then let user pick one to delete
                                try {
                                        GPConnection conn = GPConnection.getInstance(DeleteAppletActivity.this);
                                        GPTerminal term = NfcTerminal.getInstance(DeleteAppletActivity.this);

                                        if (mKeyset == null || mChannelSet == null) {
                                                Toast.makeText(DeleteAppletActivity.this,
                                                                "Keyset/Channel not available, connect card first",
                                                                Toast.LENGTH_LONG).show();
                                                return;
                                        }

                                        GPCommand cmd = new GPCommand(APDU_COMMAND.APDU_DISPLAYAPPLETS_ONCARD,
                                                        mSeekReader, null, (byte) 0, null);
                                        conn.performCommand(term, mKeyset, mChannelSet, cmd);

                                        // After listing, show the applet list for selection
                                        java.util.List<AIDRegistryEntry> registry = conn.getRegistry();
                                        if (registry == null || registry.isEmpty()) {
                                                Toast.makeText(DeleteAppletActivity.this,
                                                                "No applets found on card",
                                                                Toast.LENGTH_LONG).show();
                                                return;
                                        }

                                        // Build list of applet AIDs for selection
                                        final CharSequence[] items = new CharSequence[registry.size()];
                                        for (int i = 0; i < registry.size(); i++) {
                                                AIDRegistryEntry entry = registry.get(i);
                                                String kind = entry.getKind().toShortString();
                                                items[i] = GPUtil.byteArrayToString(entry.getAID().getBytes()) + " [" + kind + "]";
                                        }

                                        new AlertDialog.Builder(DeleteAppletActivity.this)
                                                .setTitle("Select Applet to Delete")
                                                .setItems(items, new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                                AIDRegistryEntry entry = registry.get(which);
                                                                String aidHex = GPUtil.byteArrayToString(entry.getAID().getBytes());
                                                                mEditAid.setText(aidHex);
                                                        }
                                                })
                                                .show();

                                } catch (Exception e) {
                                        Toast.makeText(DeleteAppletActivity.this,
                                                        "Error: " + e.getMessage(),
                                                        Toast.LENGTH_LONG).show();
                                }
                        }
                });
        }

        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
                return true;
        }
}
