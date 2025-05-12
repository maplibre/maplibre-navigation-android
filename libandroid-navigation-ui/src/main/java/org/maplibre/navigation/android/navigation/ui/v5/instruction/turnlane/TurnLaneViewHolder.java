package org.maplibre.navigation.android.navigation.ui.v5.instruction.turnlane;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

import org.maplibre.navigation.android.navigation.ui.v5.R;

class TurnLaneViewHolder extends RecyclerView.ViewHolder {

  TurnLaneView turnLaneView;

  TurnLaneViewHolder(View itemView) {
    super(itemView);
    turnLaneView = itemView.findViewById(R.id.turnLaneView);
  }
}
