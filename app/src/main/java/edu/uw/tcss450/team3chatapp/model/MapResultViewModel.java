package edu.uw.tcss450.team3chatapp.model;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.maps.model.LatLng;

/**
 * A ViewModel to be used in passing the result of selecting a location on the MapFragment.
 * @version 12/3/19
 */
public class MapResultViewModel extends ViewModel {
    private static MapResultViewModel mInstance;
    private MutableLiveData<LatLng> mResult;

    private MapResultViewModel () { mResult = new MutableLiveData<>();}
    public void setResult(final LatLng tRes) { mResult.setValue(tRes);}
    public MutableLiveData<LatLng> getResult() { return mResult;}

    /**
     * Factory method to provide the factory that gives access to the singleton instance.
     * @return the factory that provides the instance of this ViewModel
     */
    public static ViewModelProvider.Factory getFactory() {
        return new ViewModelProvider.Factory() {

            @NonNull
            @Override
            public MapResultViewModel create(@NonNull Class modelClass) {
                if (mInstance == null)
                    mInstance = new MapResultViewModel();
                return mInstance;
            }
        };
    }

}
