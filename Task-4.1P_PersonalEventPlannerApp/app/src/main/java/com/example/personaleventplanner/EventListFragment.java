package com.example.eventplanner;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

public class EventListFragment extends Fragment {

    private EventViewModel viewModel;
    private EventAdapter adapter;
    private RecyclerView recyclerView;
    private View layoutEmpty;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_event_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerView);
        layoutEmpty  = view.findViewById(R.id.layoutEmpty);

        // Set up ViewModel (shared with AddEditEventFragment via Activity scope)
        viewModel = new ViewModelProvider(requireActivity()).get(EventViewModel.class);

        // Set up adapter with Edit and Delete callbacks
        adapter = new EventAdapter(
            event -> {
                // Edit: navigate to AddEditEventFragment passing the event ID
                Bundle args = new Bundle();
                args.putInt("eventId", event.id);
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_list_to_edit, args);
            },
            event -> {
                // Delete: show confirmation dialog before deleting
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Delete event")
                        .setMessage("Remove \"" + event.title + "\"?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            viewModel.delete(event);
                            Snackbar.make(requireView(),
                                    "\"" + event.title + "\" deleted",
                                    Snackbar.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        );

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // Observe event list from Room via LiveData
        viewModel.allEvents.observe(getViewLifecycleOwner(), events -> {
            adapter.submitList(events);

            // Toggle empty state visibility
            if (events == null || events.isEmpty()) {
                recyclerView.setVisibility(View.GONE);
                layoutEmpty.setVisibility(View.VISIBLE);
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                layoutEmpty.setVisibility(View.GONE);
            }
        });
    }
}
