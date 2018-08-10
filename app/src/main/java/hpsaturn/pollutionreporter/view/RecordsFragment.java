package hpsaturn.pollutionreporter.view;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import hpsaturn.pollutionreporter.Config;
import hpsaturn.pollutionreporter.MainActivity;
import hpsaturn.pollutionreporter.R;
import hpsaturn.pollutionreporter.common.Storage;
import hpsaturn.pollutionreporter.models.SensorTrack;

/**
 * Created by Antonio Vanegas @hpsaturn on 10/20/15.
 */
public class RecordsFragment extends Fragment {

    private static final boolean DEBUG = Config.DEBUG;
    public static String TAG = RecordsFragment.class.getSimpleName();

    private RecyclerView mRecordsList;
    private ListRecordsAdapter mRecordsAdapter;
    private TextView mEmptyMessage;

    public static RecordsFragment newInstance() {
        RecordsFragment fragment = new RecordsFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_records, container, false);
        mEmptyMessage = (TextView)view.findViewById(R.id.tv_records_empty_list);
        mRecordsList = (RecyclerView) view.findViewById(R.id.rv_records);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 1);
        mRecordsList.setLayoutManager(gridLayoutManager);

        mRecordsAdapter = new ListRecordsAdapter();
        mRecordsAdapter.setOnItemClickListener(onItemClickListener);
        mRecordsList.setAdapter(mRecordsAdapter);

        ItemTouchHelper.Callback callback = new ItemTouchHelperCallback(mRecordsAdapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(mRecordsList);

        return view;

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public void loadData(){
        ArrayList<SensorTrack> tracks = Storage.getTracks(getActivity());
        mRecordsAdapter.updateData(tracks);
        updateUI();
    }

    public void addRecord(SensorTrack sensorTrack){
        // TODO: is necessary?
        mRecordsAdapter.addItem(0, sensorTrack);
        mRecordsList.scrollToPosition(0);
        updateUI();
    }

    public void updateRecord(SensorTrack oldRecord, SensorTrack newRecord, int position) {
        // TODO: is necessary?
        mRecordsAdapter.updateItem(position, newRecord);
        updateUI();
    }

    private void updateUI() {
        if(mRecordsAdapter.getItemCount()>0) {
            mEmptyMessage.setVisibility(View.GONE);
            mRecordsList.setVisibility(View.VISIBLE);
        }else{
            mRecordsList.setVisibility(View.GONE);
            mEmptyMessage.setVisibility(View.VISIBLE);
        }
    }

    public int getRecordsCount(){
        return mRecordsAdapter.getItemCount();
    }

    private OnItemClickListener onItemClickListener = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
            if(DEBUG) Log.d(TAG, "OnItemClickListener => Clicked: " + position + ", index " + mRecordsList.indexOfChild(view));
//            getMain().showAddDialog(mRecordsAdapter.getItem(position),position);
        }
    };

    public List<SensorTrack> getRecords() {
        return mRecordsAdapter.getRecords();
    }

    public void removeRecords() {
//        Storage.setRecords(getActivity(),new ArrayList<RecordItem>());
//        mRecordsAdapter.updateData(Storage.getRecords(getActivity()));
    }

    @Override
    public void onResume() {
        loadData();
        super.onResume();
    }

    public class ItemTouchHelperCallback extends ItemTouchHelper.Callback {

        private final ItemTouchHelperAdapter mAdapter;

        public ItemTouchHelperCallback(ItemTouchHelperAdapter adapter) {
            mAdapter = adapter;
        }

        @Override
        public boolean isLongPressDragEnabled() {
            return true;
        }

        @Override
        public boolean isItemViewSwipeEnabled() {
            return true;
        }

        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
            int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
            return makeMovementFlags(dragFlags, swipeFlags);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            mAdapter.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
            if (DEBUG) Log.d(TAG, "ItemTouchHelperCallback: onMove");
            return true;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

        }

    }

    private MainActivity getMain() {
        return ((MainActivity) getActivity());
    }


//    private void getTestData() {
//
//        addRecord(new SensorTrack("record_2018-06-24","2018.06.24","Teusaquillo"));
//        addRecord(new SensorTrack("record_2018-07-14","2018.07.14","Chapinero"));
//        addRecord(new SensorTrack("record_2018-07-24","2018.07.24","Caracas"));
//
//    }


}
