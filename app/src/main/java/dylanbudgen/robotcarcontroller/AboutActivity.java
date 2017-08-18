package dylanbudgen.robotcarcontroller;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

/**
 * About activity to show users information about the project
 * @author Dylan Budgen
 */
public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        // Set up toolbar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.about_toolbar);
        setSupportActionBar(myToolbar);

        // Add the back button to the toolbar
        myToolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        myToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        setVersionInfo();
    }


    // Required to set up toolbar and add buttons
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.generic_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    /**
     * Show version number on activity
     */
    private void setVersionInfo() {

        try {
            PackageInfo pInfo = this.getPackageManager().getPackageInfo(getPackageName(), 0);
            final String version = pInfo.versionName;
            updateTextView(R.id.textView_version, getString(R.string.version) + version);

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Update the status message text
     * @param textviewId id of text view
     * @param message string to show to user
     */
    private void updateTextView(final int textviewId, final String message) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                TextView textView = (TextView) findViewById(textviewId);
                textView.setText(message);
            }
        });
    }

}
