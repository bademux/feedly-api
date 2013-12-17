package org.github.bademux.feedly.andrss;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.github.bademux.feedly.andrss.util.FeedlyUtil;
import org.github.bademux.feedly.andrss.util.ProcessDialogAsyncTask;

import java.io.IOException;

public class MainActivity extends Activity
    implements NavigationDrawerFragment.OnFragmentInteractionListener,
               AuthInfoFragment.OnFragmentInteractionListener {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    try {
      mFeedlyUtil = new FeedlyUtil(this, getString(R.string.client_id),
                                   getString(R.string.client_secret));
    } catch (IOException e) {
      Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
    }

    setContentView(R.layout.activity_main);

    initNavigationDrawer();
  }

  private void initNavigationDrawer() {
    mTitle = getTitle();

    // Set up the drawer.
    mNavigationDrawerFragment = (NavigationDrawerFragment)
        getFragmentManager().findFragmentById(R.id.navigation_drawer);

    DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
    mNavigationDrawerFragment.setUp(R.id.navigation_drawer, drawerLayout);
  }

  @Override
  public void onNavigationDrawerItemSelected(int position) {
    // update the main content by replacing fragments
    getFragmentManager().beginTransaction().
        replace(R.id.container, getContainerFragment(position)).commit();
  }

  protected Fragment getContainerFragment(int position) {
    if (isAuthenticated()) {
      return PlaceholderFragment.newInstance(position + 1);
    }

    return new AuthInfoFragment();
  }

  @Override
  public boolean isAuthenticated() { return mFeedlyUtil.isAuthenticated(); }

  @Override
  public void onLogin() {
    try {
      if (!mFeedlyUtil.isAuthenticated()) {
        FeedlyWebAuthActivity.startActivityForResult(this, mFeedlyUtil.getRequestUrl());
      }
    } catch (IOException e) {
      Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
    }
  }

  @Override
  public void onLogout() {
    if (!mFeedlyUtil.isAuthenticated()) {
      return;
    }

    new ProcessDialogAsyncTask<Void, Void>(this) {
      @Override
      protected Void doInBackground(final Void... params) {
        try {
          mFeedlyUtil.logout();
          onNavigationDrawerItemSelected(0);
        } catch (Exception e) {
          Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
        return null;
      }
    }.execute();
  }

  // Call Back method  to get the ResponseUrl form other Activity
  @Override
  public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
    if (requestCode == FeedlyWebAuthActivity.REQUEST_CODE
        && resultCode == Activity.RESULT_OK) {
      new ProcessDialogAsyncTask<Void, Void>(this) {
        @Override
        protected Void doInBackground(final Void... params) {
          try {
            mFeedlyUtil.processResponse(FeedlyWebAuthActivity.getResponceUrl(data));
            onNavigationDrawerItemSelected(0);
          } catch (Exception e) {
            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
          }
          return null;
        }
      }.execute();
      return;
    }

    super.onActivityResult(requestCode, resultCode, data);
  }

  public void onSectionAttached(int number) {
    switch (number) {
      case 1:
        mTitle = getString(R.string.title_content);
        break;
      case 2:
        mTitle = getString(R.string.title_auth);
        break;
    }
  }

  public void restoreActionBar() {
    ActionBar actionBar = getActionBar();
    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
    actionBar.setDisplayShowTitleEnabled(true);
    actionBar.setTitle(mTitle);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    if (!mNavigationDrawerFragment.isDrawerOpen()) {
      // Only show items in the action bar relevant to this screen
      // if the drawer is not showing. Otherwise, let the drawer
      // decide what to show in the action bar.
      getMenuInflater().inflate(R.menu.main, menu);
      restoreActionBar();
      return true;
    }
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();
    if (id == R.id.action_settings) {
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  /**
   * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
   */
  private NavigationDrawerFragment mNavigationDrawerFragment;

  /**
   * Used to store the last screen title. For use in {@link #restoreActionBar()}.
   */
  private CharSequence mTitle;

  private FeedlyUtil mFeedlyUtil;

  /**
   * A placeholder fragment containing a simple view.
   */
  public static class PlaceholderFragment extends Fragment {

    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static PlaceholderFragment newInstance(int sectionNumber) {
      PlaceholderFragment fragment = new PlaceholderFragment();
      Bundle args = new Bundle();
      args.putInt(ARG_SECTION_NUMBER, sectionNumber);
      fragment.setArguments(args);
      return fragment;
    }

    public PlaceholderFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
      View rootView = inflater.inflate(R.layout.fragment_main, container, false);
      TextView textView = (TextView) rootView.findViewById(R.id.section_label);
      textView.setText(Integer.toString(getArguments().getInt(ARG_SECTION_NUMBER)));
      return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
      super.onAttach(activity);
      ((MainActivity) activity).onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER));
    }
  }
}
