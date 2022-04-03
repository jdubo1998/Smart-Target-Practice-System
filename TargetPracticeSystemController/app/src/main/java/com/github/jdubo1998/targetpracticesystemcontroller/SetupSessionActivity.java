package com.github.jdubo1998.targetpracticesystemcontroller;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.jdubo1998.targetpracticesystemcontroller.SetupTargetsAdapter.ViewHolder;

import java.util.ArrayList;

public class SetupSessionActivity extends AppCompatActivity {
    private static final String TAG = SetupSessionActivity.class.getSimpleName();
    ArrayList<String> firstTargets = new ArrayList<>();
    ArrayAdapter<String> firstTargetAdapter;

    private int targetCount = 0;
    private RecyclerView targetsRecyclerView;
    private LinearLayoutManager layoutManager;
    private SetupTargetsAdapter adapter;

    private TextView targetCountText;
    private Spinner firstTargetSpinner;
    private Spinner endMethodSpinner;
    private LinearLayout endTimeLayout;
    private LinearLayout endCountLayout;
    private EditText endTimeEditText;
    private EditText endCountEditText;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setup_session_layout);

        /* Asks for permission to use Bluetooth. */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] permissions = {Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.ACCESS_FINE_LOCATION};

            for (String permission: permissions) {
                if (checkSelfPermission(permission) == PackageManager.PERMISSION_DENIED) {
                    requestPermissions(permissions, 1);
                }
            }
        }

        targetCountText = findViewById(R.id.targetcount_text);
        firstTargetSpinner = findViewById(R.id.firsttarget_spinner);
        endMethodSpinner = findViewById(R.id.endmethod_spinner);
        endTimeLayout = findViewById(R.id.end_time);
        endCountLayout = findViewById(R.id.end_count);
        endTimeEditText = findViewById(R.id.endtime_edittext);
        endCountEditText = findViewById(R.id.endcount_edittext);

        targetsRecyclerView = findViewById(R.id.targets_recyclerview);

        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        adapter = new SetupTargetsAdapter(this);
        targetsRecyclerView.setLayoutManager(layoutManager);
        targetsRecyclerView.setAdapter(adapter);

        firstTargetAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, firstTargets);

        targetCountText.setText("Targets: 0"); // TODO: Use string resources.

        ArrayAdapter<CharSequence> endAdapter = ArrayAdapter.createFromResource(this, R.array.end_methods, android.R.layout.simple_spinner_item);
        endAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        endMethodSpinner.setAdapter(endAdapter);

        endMethodSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                endMethodSpinner.setSelection(position);

                switch (position) {
                    case 0:
                        endTimeLayout.setVisibility(View.VISIBLE);
                        endCountLayout.setVisibility(View.GONE);
                        break;
                    case 1:
                        endTimeLayout.setVisibility(View.GONE);
                        endCountLayout.setVisibility(View.VISIBLE);
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO: What is this for?
            }
        });

        firstTargetSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                firstTargetSpinner.setSelection(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                firstTargetSpinner.setSelection(0);
            }
        });

        addTarget(null); // TODO: Delete
        addTarget(null); // TODO: Delete
        addTarget(null); // TODO: Delete
        addTarget(null); // TODO: Delete
//        addTarget(null); // TODO: Delete
        endMethodSpinner.setSelection(1);
        endCountEditText.setText("15"); // TODO: Delete
    }

    public void addTarget(View view) {
        targetCount++;
        adapter.update(targetCount);
        targetCountText.setText("Targets: " + targetCount); // TODO: Use string resources.

        firstTargets.clear();
        for (int i = 1; i <= targetCount; i++) {
            firstTargets.add("" + i);
        }
        firstTargets.add("None");

        firstTargetAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        firstTargetSpinner.setAdapter(firstTargetAdapter);
    }

    public void removeTarget(View view) {
        if (targetCount > 0) {
            targetCount--;
        }

        adapter.update(targetCount);
        targetCountText.setText("Targets: " + targetCount); // TODO: Use string resources.
    }

    private String generateCode() {
        StringBuilder code = new StringBuilder();

        if (firstTargetSpinner.getSelectedItemPosition() == targetCount) {
            code.append("start;-3;;");
        } else {
            code.append("start;").append(firstTargetSpinner.getSelectedItemPosition()).append(";;");
        }

        if (endMethodSpinner.getSelectedItemPosition() == 0) {
            String param = endTimeEditText.getText().toString();
            if (param.isEmpty()) {
                return("endTimeEditText.isEmpty");
            } else {
                int seconds = (int) (Double.parseDouble(param) * 1000);
                code.append("ec;0;").append(seconds).append(";;");
            }

        } else if (endMethodSpinner.getSelectedItemPosition() == 1) {
            String param = endCountEditText.getText().toString();
            if (param.isEmpty()) {
                return("endCountEditText.isEmpty");
            } else {
                code.append("ec;1;").append(param).append(";;");
            }
        }


//        for(int i = 0; i < targetCount; i ++) {
//            adapter.setActiveUserPosition(i);
////            View viewItem = recycleView.getLayoutManager().findViewByPosition(position);
////            ViewHolder holder = (ViewHolder) targetsRecyclerView.findViewHolderForAdapterPosition(i);
//            View holder = targetsRecyclerView.getLayoutManager().findViewByPosition(i);
//
//            if (holder == null) {
//                break;
//            }
//
//            String param = holder.nextTargetSpinner.getSelectedItem().toString();
//
//            if (param.equals("None")) {
//                code.append("nt;").append(i).append(";-3");
//            } else if (param.equals("Random")) {
//                code.append("nt;").append(i).append(";-1");
//            } else {
//                int nextTarget = Integer.parseInt(param)-1;
//
//                if (nextTarget != i) {
//                    code.append("nt;").append(i).append(";").append(nextTarget);
//                } else {
//                    return "nextTarget==i";
//                }
//            }
//
//            double minUpInterval = 0.0;
//            double maxUpInterval = 0.0;
//            double minDownInterval = 0.0;
//            double maxDownInterval = 0.0;
//
//            param = holder.minUpIntervalEditText.getText().toString();
//            if (!param.isEmpty()) {
//                minUpInterval = Double.parseDouble(param);
//            }
//            param = holder.maxUpIntervalEditText.getText().toString();
//            if (!param.isEmpty()) {
//                maxUpInterval = Double.parseDouble(param);
//            }
//            param = holder.minDownIntervalEditText.getText().toString();
//            if (!param.isEmpty()) {
//                minDownInterval = Double.parseDouble(param);
//            }
//            param = holder.maxDownIntervalEditText.getText().toString();
//            if (!param.isEmpty()) {
//                maxDownInterval = Double.parseDouble(param);
//            }
//
//            if (minUpInterval != 0 || maxUpInterval != 0 || minDownInterval != 0 || maxDownInterval != 0) {
//                code.append(";ti;").append(i).append(";")
//                                .append(Math.abs((int) (minUpInterval * 1000))).append(";")
//                                .append(Math.abs((int) (minDownInterval * 1000))).append(";")
//                                .append((int) (((maxUpInterval > minUpInterval) ? maxUpInterval - minUpInterval : 0 ) * 1000)).append(";")
//                                .append((int) (((maxDownInterval > minDownInterval) ? maxDownInterval - minDownInterval : 0) * 1000));
//            }
//
//            param = holder.syncSpinner.getSelectedItem().toString();
//            if (!param.equals("None")) {
//                code.append(";sy;").append(i).append(";").append(param);
//            }
//
//            if (i < targetCount-1) {
//                code.append(";;");
//            }
//        }

        code.append("nt;0;-1;;nt;1;2;ti;2000;2000;0;0;;nt;2;0;;nt;3;2");

//        if (!((endTimeEditText.getText().toString().isEmpty() && endCountEditText.getText().toString().isEmpty()) || targetCount < 1)) {
//
//
//
//        }

//        switch (endMethod) {
//            /* End Method: Time limit */
//            case 0:
//                int endTime = (int) (Double.parseDouble(endTimeEditText.getText().toString()) * 1000);
//                code.append(endTime);
//                break;
//            /* End Method: Target limit */
//            case 1:
//                code.append(endCountEditText.getText().toString());
//                break;
//            default:
//                break;
//        }

//        for (int i = 0; i < targetCount; i++) {
//            String targetCode = generateCode((ViewHolder) targetsRecyclerView.getChildViewHolder(targetsRecyclerView.getChildAt(i)));
//
//            if (targetCode.isEmpty()) {
//                return;
//            }
//
//            code.append(";;");
//            code.append(targetCode);
//        }

//        switch(mode) {
//            /* Mode: Set Target */
//            case 0:
//                param = (String) viewHolder.nextTargetSpinner.getSelectedItem();
//
//                if (param.equals("Random")) {
//                    code = mode + ";-1";
//                } else if (param.equals("None")) {
//                    code = mode + ";-3";
//                } else {
//                    code = mode + ";" + param;
//                }
//
//                break;
//            /* Mode: Time Interval */
//            case 1:
//                if (viewHolder.upIntervalEditText.getText().toString().isEmpty() || viewHolder.upIntervalEditText.getText().toString().isEmpty()) {
//                    return "";
//                }
//
//                double param1_d = Double.parseDouble(viewHolder.upIntervalEditText.getText().toString());
//                double param2_d = Double.parseDouble(viewHolder.downIntervalEditText.getText().toString());
//
//                int param1 = (int) (param1_d * 1000);
//                int param2 = (int) (param2_d * 1000);
//
//                code = mode + ";" + param1 + ";" + param2;
//
//                break;
//            /* Mode: Sync Targets */
//            case 2:
//                param = (String) viewHolder.syncSpinner.getSelectedItem();
//
//                code = mode + ";" + param;
//                break;
//            default:
//                break;
//        }

        return code.toString();
    }

    public void toRunSession(View view) {
        String code = generateCode();

        if (code.equals("endTimeEditText.isEmpty")) {
            Log.e(TAG, "Time limit field is empty.");
            Toast.makeText(this, "Time limit field is empty.", Toast.LENGTH_SHORT).show();
        } else if (code.equals("endCountEditText.isEmpty")) {
            Log.e(TAG, "Target limit field is empty.");
            Toast.makeText(this, "Target limit field is empty.", Toast.LENGTH_SHORT).show();
        } else if (code.equals("nextTarget==i")) {
            Log.e(TAG, "Can't set next target equal to itself.");
            Toast.makeText(this, "Can't set next target equal to itself.", Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(SetupSessionActivity.this, RunSessionActivity.class);
            intent.putExtra("code", code);
            startActivity(intent);
        }
    }
}
