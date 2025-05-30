package org.maplibre.navigation.android.example;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.maplibre.android.location.permissions.PermissionsListener;
import org.maplibre.android.location.permissions.PermissionsManager;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements PermissionsListener {
    private RecyclerView recyclerView;
    private PermissionsManager permissionsManager;
    private ArrayList<SampleItem> list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // RecyclerView
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);

        // Use a linear layout manager
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // Specify an adapter
        list.add(new SampleItem(
            getString(R.string.title_mock_navigation),
            getString(R.string.description_mock_navigation),
            MockNavigationActivity.class
        ));
        list.add(new SampleItem(
            getString(R.string.title_valhalla_navigation),
            getString(R.string.description_vallhalla_navigation),
            ValhallaNavigationActivity.class
        ));
        list.add(new SampleItem(
                getString(R.string.title_graphhopper_navigation),
                getString(R.string.description_graphhopper_navigation),
                GraphHopperNavigationActivity.class
        ));
        list.add(new SampleItem(
            getString(R.string.title_navigation_ui),
            getString(R.string.description_navigation_ui),
            NavigationUIActivity.class
        ));
        list.add(new SampleItem(
            getString(R.string.title_snap_to_route),
            getString(R.string.description_snap_to_route),
            SnapToRouteNavigationActivity.class
        ));
        list.add(new SampleItem(
            getString(R.string.title_foreground_notification),
            getString(R.string.description_foreground_notification),
            NavigationWithForegroundNotificationActivity.class
        ));
        list.add(new SampleItem(
            getString(R.string.title_custom_foreground_notification),
            getString(R.string.description_custom_foreground_notification),
            NavigationWithForegroundNotificationActivity.class
        ));
        RecyclerView.Adapter adapter = new MainAdapter(list);
        recyclerView.setAdapter(adapter);

        // Check for location permission
        permissionsManager = new PermissionsManager(this);
        if (!PermissionsManager.areLocationPermissionsGranted(this)) {
            recyclerView.setEnabled(false);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(this, "This app needs location permissions in order to show its functionality.",
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            recyclerView.setEnabled(true);
        } else {
            Toast.makeText(this, "You didn't grant location permissions.",
                    Toast.LENGTH_LONG).show();
        }
    }

    /*
     * Recycler view
     */

    private class MainAdapter extends RecyclerView.Adapter<MainAdapter.ViewHolder> {

        private List<SampleItem> samples;

        class ViewHolder extends RecyclerView.ViewHolder {

            private TextView nameView;
            private TextView descriptionView;

            ViewHolder(View view) {
                super(view);
                nameView = view.findViewById(R.id.nameView);
                descriptionView = view.findViewById(R.id.descriptionView);
            }
        }

        MainAdapter(List<SampleItem> samples) {
            this.samples = samples;
        }

        @NonNull
        @Override
        public MainAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.item_main_feature, parent, false);

            view.setOnClickListener(clickedView -> {
                int position = recyclerView.getChildLayoutPosition(clickedView);
                Intent intent = new Intent(clickedView.getContext(), samples.get(position).getActivity());
                startActivity(intent);
            });

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MainAdapter.ViewHolder holder, int position) {
            holder.nameView.setText(samples.get(position).getName());
            holder.descriptionView.setText(samples.get(position).getDescription());
        }

        @Override
        public int getItemCount() {
            return samples.size();
        }
    }
}
