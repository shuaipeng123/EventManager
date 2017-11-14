package com.example.huqicheng.pm;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.huqicheng.adapter.CalendarEventListAdapter;
import com.example.huqicheng.adapter.EventListAdapter;
import com.example.huqicheng.bll.EventBiz;
import com.example.huqicheng.bll.UserBiz;
import com.example.huqicheng.entity.Event;
import com.example.huqicheng.entity.User;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CalendarFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CalendarFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CalendarFragment extends Fragment {
    private EventBiz eventBiz;
    MaterialCalendarView CalendarView;
    ListView listView;
    private CalendarEventListAdapter adapter;
    public ArrayList<Event> eventList;
    public List<CalendarDay> datesList = new ArrayList<>();
    Intent intent;
    static final String TAG="TAG";
    private User user;
    private Handler handler = null;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public CalendarFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *

     * @return A new instance of fragment CalendarFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CalendarFragment newInstance() {
        CalendarFragment fragment = new CalendarFragment();
        Bundle args = new Bundle();
        System.out.println("this is calendar fragment");
        //args.putString(ARG_PARAM1, param1);
        //args.putString(ARG_PARAM2, param2);
        //fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    /**
     * Decorate several days with a dot
     */
    public class HighlightDecorator implements DayViewDecorator {

        private int color;
        private HashSet<CalendarDay> dates;

        public HighlightDecorator(int color, Collection<CalendarDay> dates) {
            this.color = color;
            this.dates = new HashSet<>(dates);
        }

        @Override
        public boolean shouldDecorate(CalendarDay day) {
            return dates.contains(day);
        }

        @Override
        public void decorate(DayViewFacade view) {
            view.addSpan(new DotSpan(6, color));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_calendar, container, false);
        // Inflate the layout for this fragment
        CalendarView=(MaterialCalendarView)v.findViewById(R.id.calendarView);
        listView = (ListView) v.findViewById(R.id.eventlist);

        /** add decorator **/
        eventBiz = new EventBiz();
        //load user
        user = new UserBiz(getActivity()).readUser();



        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //Log.d("clicked item: ", i + "at pos " + l);
                Event event = adapter.getEventList().get(i);
                Log.d("events in CF",""+event.getDescription());
                intent = new Intent(getActivity(), DateSelected.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("event", event);//serializable
                intent.putExtras(bundle);//send data
                startActivity(intent);
            }
        });

        CalendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull com.prolificinteractive.materialcalendarview.MaterialCalendarView widget, @NonNull final com.prolificinteractive.materialcalendarview.CalendarDay date, boolean selected) {

                new Thread(){
                    @Override
                    public void run() {
                        loadEvents(user.getUserId(), date.getDate().getTime());
                    }
                }.start();
            }
        });

        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch(msg.what){
                    //get dates having events
                    case 1:
                        ArrayList<Long> dates = (ArrayList<Long>)msg.obj;
                        final List<CalendarDay> datesList = new ArrayList<>();
                        for(int i=0;i<dates.size();i++){
                            Log.d("dates",""+dates.get(i));
                            Date date = new Date(dates.get(i));
                            CalendarDay day = CalendarDay.from(date);
                            Log.e(TAG, "timestamp=" + date.toString());
                            datesList.add(day);
                        }
                        CalendarView.addDecorators(
                                new HighlightDecorator(Color.parseColor("#FF4081"),datesList )
                        );
                        break;
                    //get events on specific day
                    case 2:
                        final ArrayList<Event> events = (ArrayList<Event>)msg.obj;

                        for (int i = 0; i < events.size();i++){
                            Log.d("events",""+events.get(i).getDescription());
                        }

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                adapter = new CalendarEventListAdapter(getActivity(),null);
                                listView.setAdapter(adapter);
                                adapter.add(events);
                                adapter.notifyDataSetChanged();
                            }
                        });
                        break;
                }
            }
        };

        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        /** get data from the database**/
        new Thread(){
            @Override
            public void run() {
                loadDates(user.getUserId(),"started");
            }
        }.start();
    }

    void loadDates(long user_id, String status){
        /** add decorator **/
        EventBiz eventBiz = new EventBiz();
        List<Long> stampList = new ArrayList<>();
        try {
            stampList = eventBiz.loadDatesHavingEvents(user_id, status);
            for (int i = 0; i < stampList.size();i++){
                //Log.e(TAG, "timestamp=" + new Date(stampList.get(i)).toString());
            }
            if(stampList == null){
                return;
            }

            Message msg = Message.obtain();
            msg.what = 1;
            msg.obj = stampList;
            handler.handleMessage(msg);

        }catch (Exception e){
            e.printStackTrace();
        }
    }
    void loadEvents(long user_id, long time_stamp){
        /** add decorator **/
        EventBiz eventBiz = new EventBiz();
        List<Event> eventList = new ArrayList<>();
        try {
            eventList = eventBiz.loadEventsOfOneDate(user_id, time_stamp);
            if(eventList == null){
                return;
            }
            for (int i = 0; i < eventList.size();i++){
                Log.e(TAG, "event=" + eventList.get(i));
            }

            Message msg = Message.obtain();
            msg.what = 2;
            msg.obj = eventList;
            handler.handleMessage(msg);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
