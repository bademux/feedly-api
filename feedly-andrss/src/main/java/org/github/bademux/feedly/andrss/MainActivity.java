/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *   Contributors:
 *                Bademus
 */

package org.github.bademux.feedly.andrss;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import org.github.bademux.feedly.andrss.util.FeedlyUtil;
import org.github.bademux.feedly.andrss.util.ProcessDialogAsyncTask;

import java.io.IOException;

public class MainActivity extends Activity
    implements NavigationDrawerFragment.OnFragmentInteractionListener,
               AuthInfoFragment.OnFragmentInteractionListener,
               FeedListFragment.OnFragmentInteractionListener {

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
      return new FeedListFragment();
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
          } catch (Exception e) {
            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
          }
          return null;
        }
        @Override
        protected void onPostExecute(Void result) {
          onNavigationDrawerItemSelected(0);
          super.onPostExecute(result);
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

  @Override
  public void onFragmentInteraction(final String id) {
// TODO: Implement
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
}
