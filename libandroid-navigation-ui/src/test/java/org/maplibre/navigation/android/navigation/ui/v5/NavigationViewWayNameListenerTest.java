package org.maplibre.navigation.android.navigation.ui.v5;

import org.maplibre.navigation.android.navigation.ui.v5.NavigationPresenter;
import org.maplibre.navigation.android.navigation.ui.v5.NavigationViewWayNameListener;
import org.maplibre.navigation.android.navigation.ui.v5.map.OnWayNameChangedListener;

import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class NavigationViewWayNameListenerTest {

  @Test
  public void onWayNameChanged_presenterReceivesNewWayName() {
    NavigationPresenter presenter = mock(NavigationPresenter.class);
    String newWayName = "New way name";
    OnWayNameChangedListener listener = new NavigationViewWayNameListener(presenter);

    listener.onWayNameChanged(newWayName);

    verify(presenter).onWayNameChanged(newWayName);
  }
}