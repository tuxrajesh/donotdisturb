package raj.apps.donotdisturb;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

/**
 * Widget definition that is used to enable/disable do not disturb mode
 */
public class DoNotDisturbWidget extends AppWidgetProvider {

	@Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        
		final int numberOfWidgets = appWidgetIds.length;
		
		for(int i = 0; i < numberOfWidgets;i++){
			
			int appWidgetId = appWidgetIds[i];
			
			Intent intent = new Intent(context, DoNotDisturbActivity.class);
			PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
			
			RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.donotdisturb_widget);
			views.setOnClickPendingIntent(R.id.swt_enable, pendingIntent);
			
			appWidgetManager.updateAppWidget(appWidgetId, views);
		}

    }

}
