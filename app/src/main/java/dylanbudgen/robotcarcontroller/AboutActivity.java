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

    private void setVersionInfo() {

        try {
            PackageInfo pInfo = this.getPackageManager().getPackageInfo(getPackageName(), 0);
            final String version = pInfo.versionName;

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    TextView textViewVersion = (TextView) findViewById(R.id.textView_version);
                    textViewVersion.setText("v" + version);
                }
            });

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

    }

}
