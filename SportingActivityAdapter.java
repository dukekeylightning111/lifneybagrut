package com.example.mysportfriends_school_project;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputLayout;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SportingActivityAdapter extends RecyclerView.Adapter<SportingActivityAdapter.ViewHolder> {
    private List<SportingActivityClass> sportingActivities = new ArrayList<>();
    private Customer customer;
    private SportingActivityClassDao sportingActivityClassDao;
    private int customerId;
    private Context context;
    private DoubleClickListener doubleClickListener;
    private View.OnLongClickListener onLongClickListener;

    public void deleteItem(int position) {
        sportingActivities.remove(position);
        notifyItemRemoved(position);
    }

    public SportingActivityAdapter(Customer customer, SportingActivityClassDao sportingActivityClassDao, Activity activity, Context context) {
        this.customer = customer;
        this.customerId = customer.getId();
        this.sportingActivityClassDao = sportingActivityClassDao;
        this.context = context;
        loadSportingActivities();
        onLongClickListener = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                int position = (int) view.getTag();
                SportingActivityClass sportingActivity = getSportingActivityAtPosition(position);
                editSportingActivity(sportingActivity, activity, customer, position);
                return true;
            }
        };


        doubleClickListener = new DoubleClickListener(new DoubleClickListener.OnDoubleClickListener() {
            @Override
            public void onDoubleClick(View view) {
                int position = (int) view.getTag();
                SportingActivityClass sportingActivity = getSportingActivityAtPosition(position);
                String message = customer.getName().toString() + " הזמין אותכם! \n" + " "
                        + ":פעולת ספורט\n"  + sportingActivity.getTitle()
                        + "\nזמן: " + sportingActivity.getTime() +  " " +  sportingActivity.getDate()
                        + "\nקטגוריה" + sportingActivity.getCategory()
                        + "\nמיקום: " + sportingActivity.getLocation()
                        + "\nתיאור: " + sportingActivity.getDescription();

                showConfirmationDialog(message);
            }
        });
    }
    private void editSportingActivity(final SportingActivityClass sportingActivity, Activity activity, Customer currentCustomer, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("ערכו פעולת ספורט:");
        TextInputLayout titleLayout = new TextInputLayout(activity);
        titleLayout.setHint("כותרת");
        EditText editTitle = new EditText(activity);
        titleLayout.addView(editTitle);
        TextInputLayout descriptionLayout = new TextInputLayout(activity);
        descriptionLayout.setHint("תיאור");
        EditText editDescription = new EditText(activity);
        descriptionLayout.addView(editDescription);
        TextInputLayout timeLayout = new TextInputLayout(activity);
        timeLayout.setHint("זמן");
        EditText editTime = new EditText(activity);
        timeLayout.addView(editTime);
        TextInputLayout dateLayout = new TextInputLayout(activity);
        dateLayout.setHint("תאריך");
        EditText editDate = new EditText(activity);
        dateLayout.addView(editDate);
        TextInputLayout locLayout = new TextInputLayout(activity);
        locLayout.setHint("מיקום");
        EditText editLoc = new EditText(activity);
        locLayout.addView(editLoc);

        editTime.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5), new TimeInputFilter()});
        editDate.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10), new DateInputFilter()});

        editTitle.setText(sportingActivity.getTitle());
        editDescription.setText(sportingActivity.getDescription());
        editTime.setText(sportingActivity.getTime());
        editDate.setText(sportingActivity.getDate());
        editLoc.setText(sportingActivity.getLocation());
        LinearLayout dialogLayout = new LinearLayout(activity);
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.addView(titleLayout);
        dialogLayout.addView(descriptionLayout);
        dialogLayout.addView(timeLayout);
        dialogLayout.addView(dateLayout);
        dialogLayout.addView(locLayout);
        builder.setView(dialogLayout);

        builder.setPositiveButton("אישור", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newTitle = editTitle.getText().toString();
                String newDescription = editDescription.getText().toString();
                String newTime = editTime.getText().toString();
                String newDate = editDate.getText().toString();
                String newLoc = editLoc.getText().toString();
                if (newTitle.isEmpty() || newDescription.isEmpty() || newTime.isEmpty() || newDate.isEmpty()) {
                    Toast.makeText(activity, "נא למלא את כל השדות", Toast.LENGTH_LONG).show();
                } else {
                    sportingActivity.setTitle(newTitle);
                    sportingActivity.setDescription(newDescription);
                    sportingActivity.setTime(newTime);
                    sportingActivity.setDate(newDate);
                    sportingActivity.setLocation(newLoc);
                    if (position != RecyclerView.NO_POSITION) {
                        Thread updateThread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                sportingActivityClassDao.update(sportingActivity);
                                sportingActivities.set(position, sportingActivity);
                            }
                        });
                        updateThread.start();
                        notifyItemChanged(position);
                    }

                }
            }
        });

        builder.setNegativeButton("ביטול", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showConfirmationDialog(final String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("אשרו בחירת פעולה לפני השיתוף")
                .setMessage("אתם מאשרים שיתוף לפעולה זו? (ההודעה תראה כך:)" + message)
                .setPositiveButton("אישור", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        showShareDialog(message);
                    }
                })
                .setNegativeButton("ביטול", null)
                .show();
    }

    private void showShareDialog(final String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("שתפו פעולה")
                .setMessage("איך תרצו לשתף פעולה זו?")
                .setPositiveButton("הודעת SMS?", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        sendSMS(message);
                    }
                })
                .show();
    }

    private void sendSMS(String message) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("smsto:"));
        intent.putExtra("sms_body", message);
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        }
    }
    public void clearSportingActivities() {
        sportingActivities.clear();
        notifyDataSetChanged();
    }

    public void setSportingActivities(List<SportingActivityClass> sportingActivities) {
        this.sportingActivities.clear();

        if (sportingActivities != null) {
            this.sportingActivities.addAll(sportingActivities);
        }

        notifyDataSetChanged();
    }

    public SportingActivityClass getSportingActivityAtPosition(int position) {
        if (position >= 0 && position < sportingActivities.size()) {
            return sportingActivities.get(position);
        } else {
            return null;
        }
    }

    public void removeSportingActivity(int position) {
        sportingActivities.remove(position);
        notifyItemRemoved(position);
    }



    public void deleteSportingActivity(SportingActivityClass sportingActivityClass, SportingActivityClassDao sportingActivityClassDao) {
        sportingActivityClassDao.delete(sportingActivityClass);
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sporting_acitivity_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        view.setOnClickListener(doubleClickListener);
        view.setOnLongClickListener(onLongClickListener);
        return viewHolder;
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(sportingActivities.get(position));


    }

    @Override
    public int getItemCount() {
        return sportingActivities.size();
    }

    private void loadSportingActivities() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final List<SportingActivityClass> sportingActivities = sportingActivityClassDao.getSportingActivitiesByUserId(customerId);
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        setSportingActivities(sportingActivities);
                    }
                });
            }
        }).start();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView nameTextView;
        private TextView timeTextView;
        private TextView locationTextView;
        private TextView activityDescTextView;
        private ImageView imageViewCategory;
        private Context context;

        public ViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.sport_activity_name);
            timeTextView = itemView.findViewById(R.id.sport_activity_time);
            locationTextView = itemView.findViewById(R.id.sport_activity_location);
            activityDescTextView = itemView.findViewById(R.id.act_desc);
            imageViewCategory = itemView.findViewById(R.id.imageViewCategory);
            context = itemView.getContext();
        }

        public void bind(@NonNull SportingActivityClass sportingActivity) {
            if (sportingActivity != null) {
                String name = sportingActivity.getTitle();
                String time = sportingActivity.getTime();
                String location = sportingActivity.getLocation();
                locationTextView.setText("מיקום הפעולה: " + location);
                String description = sportingActivity.getDescription();
                Customer customer = sportingActivity.getCustomer();

                nameTextView.setText(name);
                timeTextView.setText(sportingActivity.getDate() + " " + time);
                activityDescTextView.setText(description);
                imageViewCategory.setScaleType(ImageView.ScaleType.CENTER_CROP);

                String category = sportingActivity.getCategory();
                if (category.equals("football")) {
                    imageViewCategory.setImageResource(R.drawable.people_football_image);
                } else if (category.equals("basketball")) {
                    imageViewCategory.setImageResource(R.drawable.basketbal_in_activity);
                } else if (category.equals("tennis")) {
                    imageViewCategory.setImageResource(R.drawable.tennis_in_activity_wallpaper);
                } else if (category.equals("volleyball")) {
                    imageViewCategory.setImageResource(R.drawable.people_volleyval_image);
                }

                itemView.setTag(getAdapterPosition());
            } else{
                nameTextView.setText("לא יצרתם פעולות ספורט");
            }
        }
    }
}

