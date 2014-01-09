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

import android.app.Activity;
import android.app.ListFragment;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A fragment representing a list of Items.
 * <p />
 * <p />
 * Activities containing this fragment MUST implement the {@link org.github.bademux.feedly.andrss.FeedListFragment.OnFragmentInteractionListener}
 * interface.
 */
public class FeedListFragment extends ListFragment {

  // TODO: Rename parameter arguments, choose names that match
  // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
  private static final String ARG_PARAM1 = "param1";
  private static final String ARG_PARAM2 = "param2";

  // TODO: Rename and change types of parameters
  private String mParam1;
  private String mParam2;

  private OnFragmentInteractionListener mListener;

  // TODO: Rename and change types of parameters
  public static FeedListFragment newInstance(String param1, String param2) {
    FeedListFragment fragment = new FeedListFragment();
    Bundle args = new Bundle();
    args.putString(ARG_PARAM1, param1);
    args.putString(ARG_PARAM2, param2);
    fragment.setArguments(args);
    return fragment;
  }

  /**
   * Mandatory empty constructor for the fragment manager to instantiate the
   * fragment (e.g. upon screen orientation changes).
   */
  public FeedListFragment() {
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (getArguments() != null) {
      mParam1 = getArguments().getString(ARG_PARAM1);
      mParam2 = getArguments().getString(ARG_PARAM2);
    }

    // TODO: Change Adapter to display your content
    setListAdapter(new ArrayAdapter<DummyContent.DummyItem>(getActivity(),
                                                            android.R.layout.simple_list_item_1,
                                                            android.R.id.text1,
                                                            DummyContent.ITEMS));
  }


  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    try {
      mListener = (OnFragmentInteractionListener) activity;
    } catch (ClassCastException e) {
      throw new ClassCastException(activity.toString()
                                   + " must implement OnFragmentInteractionListener");
    }
  }

  @Override
  public void onDetach() {
    super.onDetach();
    mListener = null;
  }


  @Override
  public void onListItemClick(ListView l, View v, int position, long id) {
    super.onListItemClick(l, v, position, id);

    if (null != mListener) {
      // Notify the active callbacks interface (the activity, if the
      // fragment is attached to one) that an item has been selected.
      mListener.onFragmentInteraction(DummyContent.ITEMS.get(position).id);
    }
  }

  /**
   * This interface must be implemented by activities that contain this
   * fragment to allow an interaction in this fragment to be communicated
   * to the activity and potentially other fragments contained in that
   * activity.
   * <p>
   * See the Android Training lesson <a href=
   * "http://developer.android.com/training/basics/fragments/communicating.html"
   * >Communicating with Other Fragments</a> for more information.
   */
  public interface OnFragmentInteractionListener {

    // TODO: Update argument type and TBL_NAME
    public void onFragmentInteraction(String id);
  }
}


/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
class DummyContent {

  /**
   * An array of sample (dummy) items.
   */
  public static List<DummyItem> ITEMS = new ArrayList<DummyItem>();

  /**
   * A map of sample (dummy) items, by ID.
   */
  public static Map<String, DummyItem> ITEM_MAP = new HashMap<String, DummyItem>();

  static {
    // Add 3 sample items.
    addItem(new DummyItem("1", "Item 1"));
    addItem(new DummyItem("2", "Item 2"));
    addItem(new DummyItem("3", "Item 3"));
  }

  private static void addItem(DummyItem item) {
    ITEMS.add(item);
    ITEM_MAP.put(item.id, item);
  }

  /**
   * A dummy item representing a piece of content.
   */
  public static class DummyItem {
    public String id;
    public String content;

    public DummyItem(String id, String content) {
      this.id = id;
      this.content = content;
    }

    @Override
    public String toString() {
      return content;
    }
  }
}

