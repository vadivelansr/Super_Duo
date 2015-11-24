package barqsoft.footballscores.widget;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.R;
import barqsoft.footballscores.Utilities;

/**
 * Created by vadivelansr on 11/24/2015.
 */

public class ScoreWidgetService extends RemoteViewsService {
    String homeScore = "";
    String awayScore = "";

    private static final String[] SCORE_COLUMNS = {
            DatabaseContract.scores_table.DATE_COL,
            DatabaseContract.scores_table.HOME_COL,
            DatabaseContract.scores_table.AWAY_COL,
            DatabaseContract.scores_table.HOME_GOALS_COL,
            DatabaseContract.scores_table.AWAY_GOALS_COL,
            DatabaseContract.scores_table.MATCH_ID,
            DatabaseContract.scores_table.LEAGUE_COL
    };

    public static final int COL_HOME = 1;
    public static final int COL_AWAY = 2;
    public static final int COL_HOME_GOALS = 3;
    public static final int COL_AWAY_GOALS = 4;

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor data = null;
            @Override
            public void onCreate() {

            }

            @Override
            public void onDataSetChanged() {
                if(data != null){
                    data.close();
                }
                final long identityToken = Binder.clearCallingIdentity();

                Uri dateUri = DatabaseContract.scores_table.buildScoreWithDate();
                String[] date = new String[1];
                date[0] = Utilities.getFormattedDate(0);
                data = getContentResolver().query(dateUri, SCORE_COLUMNS, DatabaseContract.scores_table.DATE_COL, date, null);

                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if(data != null){
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if(position == AdapterView.INVALID_POSITION || data == null
                        || !data.moveToPosition(position)){
                    return null;
                }
                RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget_scores_list_item);
                String homeName = data.getString(COL_HOME);
                String awayName = data.getString(COL_AWAY);
                int homeGoals = data.getInt(COL_HOME_GOALS);
                int awayGoals = data.getInt(COL_AWAY_GOALS);

                views.setTextViewText(R.id.home_name, homeName);
                views.setTextViewText(R.id.away_name, awayName);

                if(homeGoals != -1){
                    homeScore += homeGoals;
                    awayScore += awayGoals;
                }else{
                    homeScore = "-";
                    awayScore = "-";
                }
                views.setTextViewText(R.id.home_score, homeScore);
                views.setTextViewText(R.id.away_score, awayScore);

                final Intent intent = new Intent();
                Uri dateUri = DatabaseContract.scores_table.buildScoreWithDate();
                intent.setData(dateUri);
                views.setOnClickFillInIntent(R.id.widget_list_item, intent);

                return views;
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_scores_list_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if(data.moveToPosition(position))
                    return data.getLong(1);
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
