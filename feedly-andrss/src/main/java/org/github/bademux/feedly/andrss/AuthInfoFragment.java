package org.github.bademux.feedly.andrss;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


/** Shows login proposition */
public class AuthInfoFragment extends Fragment {

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    return inflater.inflate(R.layout.fragment_auth_info, container, false);
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    Button btn = (Button) view.findViewById(R.id.button_login);

    btn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (mListener != null) {
          mListener.onLogin();
        }
      }
    });
    super.onViewCreated(view, savedInstanceState);
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

  public AuthInfoFragment() {}

  private OnFragmentInteractionListener mListener;

  public interface OnFragmentInteractionListener {

    public void onLogin();
  }
}
