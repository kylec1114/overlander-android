package hk.overlander.journal;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class MainActivity extends Activity {
  private static final int REQUEST_PHOTO = 8;
  private static final int REQUEST_MUSIC = 9;
  private static final int REQUEST_NOTIFICATIONS = 12;

  private LinearLayout page;
  private LinearLayout body;
  private JSONArray tasks = new JSONArray();
  private SharedPreferences prefs;
  private String tab = "車票";
  private String scene = "黃昏";
  private String photoId;
  private MediaPlayer music;

  private final int ink = Color.rgb(48, 54, 50);
  private final int green = Color.rgb(64, 92, 74);
  private final int cream = Color.rgb(245, 239, 223);

  private int dp(float value) {
    return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    prefs = getSharedPreferences("journey", MODE_PRIVATE);

    try {
      tasks = new JSONArray(prefs.getString("tasks", "[]"));
    } catch (Exception error) {
      toast("紀錄未能載入；請勿清除應用程式資料");
    }

    scene = prefs.getString("scene", "黃昏");
    getWindow().setStatusBarColor(green);
    getWindow().setNavigationBarColor(green);
    render();

    String today = formatDate(System.currentTimeMillis());
    if (!today.equals(prefs.getString("boarding", ""))) {
      new AlertDialog.Builder(this)
          .setTitle("今日乘車證 · " + today)
          .setItems(new String[]{"輕鬆慢行", "專心一程", "休息充電"}, (dialog, which) -> {
            prefs.edit().putString("boarding", today).apply();
            toast("今日，由一小步開始。");
          })
          .setNegativeButton("直接上車", (dialog, which) -> prefs.edit().putString("boarding", today).apply())
          .show();
    }
  }

  private String formatDate(long time) {
    return new SimpleDateFormat("yyyy-MM-dd", Locale.TRADITIONAL_CHINESE).format(new Date(time));
  }

  private void toast(String message) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
  }

  private void saveTasks() {
    prefs.edit().putString("tasks", tasks.toString()).apply();
  }

  private TextView text(String value, int size) {
    TextView view = new TextView(this);
    view.setText(value);
    view.setTextSize(size);
    view.setTextColor(ink);
    view.setPadding(dp(5), dp(6), dp(5), dp(6));
    return view;
  }

  private Button button(String label, View.OnClickListener listener) {
    Button view = new Button(this);
    view.setText(label);
    view.setAllCaps(false);
    view.setTextColor(green);
    view.setOnClickListener(listener);
    return view;
  }

  private void render() {
    page = new LinearLayout(this);
    page.setOrientation(LinearLayout.VERTICAL);
    page.setBackgroundColor(cream);
    page.setPadding(dp(14), 0, dp(14), 0);
    setContentView(page);

    page.setOnApplyWindowInsetsListener((view, insets) -> {
      view.setPadding(dp(14), insets.getSystemWindowInsetTop(), dp(14), insets.getSystemWindowInsetBottom());
      return insets;
    });
    page.requestApplyInsets();

    page.addView(text("THE OVERLANDER / 漫遊日誌", 19));

    View scenery = new SceneryView();
    page.addView(scenery, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(145)));

    page.addView(text("把做過的事，變成走過的路。", 14));

    LinearLayout navigation = new LinearLayout(this);
    for (String item : new String[]{"車票", "日誌", "收藏", "聲音"}) {
      navigation.addView(button(item, v -> {
        tab = item;
        render();
      }), new LinearLayout.LayoutParams(0, dp(48), 1f));
    }
    page.addView(navigation);

    ScrollView scroll = new ScrollView(this);
    body = new LinearLayout(this);
    body.setOrientation(LinearLayout.VERTICAL);
    scroll.addView(body);
    page.addView(scroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

    if (tab.equals("車票")) {
      showTickets();
    } else if (tab.equals("日誌")) {
      showJournal();
    } else if (tab.equals("收藏")) {
      showCollection();
    } else {
      showSound();
    }

    page.addView(text("本機儲存 · 測試版 · 尚未提供備份", 11));
  }

  private void showTickets() {
    body.addView(button("＋ 開一張車票", v -> editTicket(null, false)));
    body.addView(button("記低已經做過嘅事", v -> editTicket(null, true)));

    int pendingCount = 0;
    for (int index = 0; index < tasks.length(); index++) {
      JSONObject task = tasks.optJSONObject(index);
      if (task != null && !task.optBoolean("done")) {
        showTicket(task);
        pendingCount++;
      }
    }

    if (pendingCount == 0) {
      body.addView(text("車票仲未寫好，下一站由你決定。", 18));
    }

    body.addView(button("今日收車", v -> {
      int completedToday = 0;
      String today = formatDate(System.currentTimeMillis());
      for (int index = 0; index < tasks.length(); index++) {
        JSONObject task = tasks.optJSONObject(index);
        if (task.optBoolean("done") && today.equals(formatDate(task.optLong("completed")))) {
          completedToday++;
        }
      }

      new AlertDialog.Builder(this)
          .setTitle("收好今日")
          .setMessage("今日留下 " + completedToday + " 張票尾。\n未行完嘅路，可以下次再行。")
          .setPositiveButton("翻開日誌", (dialog, which) -> {
            tab = "日誌";
            render();
          })
          .setNegativeButton("留喺車廂", null)
          .show();
    }));
  }

  private void editTicket(JSONObject existing, boolean alreadyCompleted) {
    LinearLayout form = new LinearLayout(this);
    form.setOrientation(LinearLayout.VERTICAL);
    form.setPadding(dp(20), 0, dp(20), 0);

    EditText titleInput = new EditText(this);
    titleInput.setHint("目的地 / 任務名稱");

    EditText noteInput = new EditText(this);
    noteInput.setHint("票背留一句（可留空）");

    if (existing != null) {
      titleInput.setText(existing.optString("title"));
      noteInput.setText(existing.optString("note"));
    }

    form.addView(titleInput);
    form.addView(noteInput);

    AlertDialog dialog = new AlertDialog.Builder(this)
        .setTitle("寫一張車票")
        .setView(form)
        .setPositiveButton("收好", null)
        .setNegativeButton("取消", null)
        .create();

    dialog.setOnShowListener(listener -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
      String title = titleInput.getText().toString().trim();
      if (title.isEmpty()) {
        titleInput.setError("請寫低目的地");
        return;
      }

      try {
        JSONObject task = existing == null ? new JSONObject() : existing;
        task.put("title", title);
        task.put("note", noteInput.getText().toString());

        if (existing == null) {
          task.put("id", UUID.randomUUID().toString());
          task.put("done", alreadyCompleted);
          if (alreadyCompleted) {
            task.put("completed", System.currentTimeMillis());
          }
          tasks.put(task);
        }

        saveTasks();
        dialog.dismiss();
        render();
      } catch (Exception error) {
        toast("未能儲存，請重試");
      }
    }));

    dialog.show();
  }

  private void showTicket(JSONObject task) {
    boolean completed = task.optBoolean("done");

    LinearLayout card = new LinearLayout(this);
    card.setOrientation(LinearLayout.VERTICAL);
    card.setPadding(dp(13), dp(9), dp(13), dp(9));

    GradientDrawable background = new GradientDrawable();
    background.setColor(Color.rgb(255, 250, 235));
    background.setCornerRadius(dp(12));
    background.setStroke(dp(1), Color.rgb(216, 198, 168));
    card.setBackground(background);

    LinearLayout.LayoutParams cardLayout = new LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    cardLayout.setMargins(0, dp(8), 0, dp(8));
    body.addView(card, cardLayout);

    card.addView(text(completed ? "已到站 · " + formatDate(task.optLong("completed")) : "自由車票 · 不限班次", 12));

    TextView title = text(task.optString("title"), 22);
    title.setTypeface(null, 1);
    card.addView(title);

    String note = task.optString("note");
    if (!note.isEmpty()) {
      card.addView(text(note, 15));
    }

    String photo = task.optString("photo");
    if (!photo.isEmpty()) {
      try {
        ImageView imageView = new ImageView(this);
        imageView.setImageURI(Uri.parse(photo));
        imageView.setAdjustViewBounds(true);
        imageView.setMaxHeight(dp(180));
        card.addView(imageView);
      } catch (Exception error) {
        card.addView(text("相片暫時無法開啟", 12));
      }
    }

    LinearLayout actions = new LinearLayout(this);
    actions.addView(button(completed ? "復原完成" : "驗票 · 完成", v -> {
      try {
        task.put("done", !completed);
        if (!completed) {
          task.put("completed", System.currentTimeMillis());
          cancelReminder(task);
          toast("已蓋日期印：呢一程，你行過喇。");
        }
        saveTasks();
        render();
      } catch (Exception error) {
        toast("未能儲存");
      }
    }));

    actions.addView(button("票背 / 更多", v -> showTaskMenu(task)));
    card.addView(actions);
  }

  private void showTaskMenu(JSONObject task) {
    new AlertDialog.Builder(this)
        .setTitle(task.optString("title"))
        .setItems(new String[]{
            "修改 / 留一句",
            "開始呢一程",
            "加相片",
            "提醒我",
            "取消提醒",
            "加入手機日曆",
            "刪除車票"
        }, (dialog, which) -> {
          if (which == 0) {
            editTicket(task, false);
          } else if (which == 1) {
            new AlertDialog.Builder(this)
                .setTitle("本程目的地")
                .setMessage(task.optString("title") + "\n\n慢慢行，唔需要倒數。")
                .setPositiveButton("繼續旅程", null)
                .show();
          } else if (which == 2) {
            photoId = task.optString("id");
            Intent picker = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            picker.setType("image/*");
            picker.addCategory(Intent.CATEGORY_OPENABLE);
            picker.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            try {
              startActivityForResult(picker, REQUEST_PHOTO);
            } catch (ActivityNotFoundException error) {
              toast("未找到相片選擇工具");
            }
          } else if (which == 3) {
            requestReminder(task);
          } else if (which == 4) {
            cancelReminder(task);
            toast("已取消提醒");
          } else if (which == 5) {
            try {
              Intent calendar = new Intent(Intent.ACTION_INSERT)
                  .setData(CalendarContract.Events.CONTENT_URI)
                  .putExtra(CalendarContract.Events.TITLE, task.optString("title"))
                  .putExtra(CalendarContract.Events.DESCRIPTION, task.optString("note"));
              startActivity(calendar);
            } catch (ActivityNotFoundException error) {
              toast("未找到可新增行程嘅日曆 App");
            }
          } else if (which == 6) {
            new AlertDialog.Builder(this)
                .setMessage("刪除呢張車票？")
                .setNegativeButton("取消", null)
                .setPositiveButton("刪除", (confirmDialog, whichButton) -> {
                  cancelReminder(task);
                  for (int index = tasks.length() - 1; index >= 0; index--) {
                    if (tasks.optJSONObject(index).optString("id").equals(task.optString("id"))) {
                      tasks.remove(index);
                    }
                  }
                  saveTasks();
                  render();
                })
                .show();
          }
        })
        .show();
  }

  private Intent reminderIntent(JSONObject task) {
    Intent intent = new Intent(this, ReminderReceiver.class);
    intent.setData(Uri.parse("overlander://reminder/" + task.optString("id")));
    intent.putExtra("id", task.optString("id"));
    return intent;
  }

  private void cancelReminder(JSONObject task) {
    PendingIntent pendingIntent = PendingIntent.getBroadcast(
        this, 0, reminderIntent(task), PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);

    if (pendingIntent != null) {
      ((android.app.AlarmManager) getSystemService(ALARM_SERVICE)).cancel(pendingIntent);
      pendingIntent.cancel();
    }

    ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).cancel(task.optString("id").hashCode());
  }

  private void requestReminder(JSONObject task) {
    if (task.optBoolean("done")) {
      toast("已完成嘅車票唔需要提醒");
      return;
    }

    if (Build.VERSION.SDK_INT >= 33
        && checkSelfPermission("android.permission.POST_NOTIFICATIONS") != PackageManager.PERMISSION_GRANTED) {
      requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"}, REQUEST_NOTIFICATIONS);
      toast("授權後請再選擇提醒時間");
      return;
    }

    new AlertDialog.Builder(this)
        .setTitle("幾時提醒？（約略時間）")
        .setItems(new String[]{"15 分鐘後", "1 小時後", "明日呢個時間"}, (dialog, which) -> {
          long delay = new long[]{900000L, 3600000L, 86400000L}[which];
          PendingIntent pendingIntent = PendingIntent.getBroadcast(
              this, 0, reminderIntent(task), PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

          try {
            ((android.app.AlarmManager) getSystemService(ALARM_SERVICE))
                .setAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + delay, pendingIntent);
            toast("已安排約略提醒；重啟手機後需重新設定");
          } catch (Exception error) {
            toast("提醒未能安排");
          }
        })
        .show();
  }

  private void showJournal() {
    int completedCount = 0;
    for (int index = tasks.length() - 1; index >= 0; index--) {
      JSONObject task = tasks.optJSONObject(index);
      if (task != null && task.optBoolean("done")) {
        showTicket(task);
        completedCount++;
      }
    }

    if (completedCount == 0) {
      body.addView(text("第一個足印，會由一件小事開始。", 18));
    }
  }

  private void showCollection() {
    int completedCount = 0;
    for (int index = 0; index < tasks.length(); index++) {
      if (tasks.optJSONObject(index).optBoolean("done")) {
        completedCount++;
      }
    }

    body.addView(text("旅程收藏 · 已完成 " + completedCount + " 程", 22));
    body.addView(text("虛構旅程站點，唔代表真實到訪紀錄。", 12));

    String[] stations = {"山間小站", "海邊月台", "雨中小鎮", "森林支線", "夜行終點"};
    for (int index = 0; index < stations.length; index++) {
      int required = (index + 1) * 3;
      body.addView(text((completedCount >= required ? "已收藏：" : "未到達：") + stations[index] + " · " + required + " 程", 19));
    }

    body.addView(text("冇連勝，冇扣分；復原完成會同步更新。", 13));
  }

  private void showSound() {
    body.addView(text("窗邊收音機", 24));
    body.addView(text("原創程序合成車輪聲 · 非實地錄音", 13));
    body.addView(button("播放 / 暫停火車聲", v -> startRailSound()));

    SeekBar volume = new SeekBar(this);
    volume.setProgress(35);
    volume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (railSound != null) {
          railSound.setVolume(progress / 100f, progress / 100f);
        }
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {
      }

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {
      }
    });
    body.addView(volume);

    body.addView(button("選擇自己嘅音樂", v -> {
      try {
        Intent picker = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        picker.setType("audio/*");
        picker.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(picker, REQUEST_MUSIC);
      } catch (ActivityNotFoundException error) {
        toast("未找到音樂選擇工具");
      }
    }));

    body.addView(button("播放 / 暫停音樂", v -> {
      if (music == null) {
        toast("請先選擇音樂");
        return;
      }

      if (music.isPlaying()) {
        music.pause();
      } else {
        music.start();
      }
    }));

    body.addView(text("離開 App 會暫停聲音；本版未有背景播放。", 12));

    for (String option : new String[]{"晨光", "黃昏", "夜行"}) {
      body.addView(button(option + "窗景", v -> {
        scene = option;
        prefs.edit().putString("scene", option).apply();
        render();
      }));
    }
  }

  private android.media.AudioTrack railSound;

  private void startRailSound() {
    try {
      if (railSound == null) {
        int sampleRate = 22050;
        int length = sampleRate * 6;
        short[] samples = new short[length];
        java.util.Random random = new java.util.Random(41);
        double low = 0;

        for (int index = 0; index < length; index++) {
          double time = index / (double) sampleRate;
          double noise = random.nextDouble() * 2 - 1;
          low = 0.96 * low + 0.04 * noise;

          double cycle = time % 0.75;
          double envelope = Math.exp(-cycle * 34)
              + (cycle >= 0.16 ? 0.65 * Math.exp(-(cycle - 0.16) * 34) : 0);

          double value = 0.18 * low
              + 0.22 * envelope * Math.sin(2 * Math.PI * 92 * time)
              + 0.025 * noise;

          double edge = Math.min(1, Math.min(time / 0.03, (6 - time) / 0.03));
          samples[index] = (short) (Math.tanh(value) * 20000 * edge);
        }

        railSound = new android.media.AudioTrack.Builder()
            .setAudioAttributes(new android.media.AudioAttributes.Builder()
                .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
                .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC)
                .build())
            .setAudioFormat(new android.media.AudioFormat.Builder()
                .setSampleRate(sampleRate)
                .setEncoding(android.media.AudioFormat.ENCODING_PCM_16BIT)
                .setChannelMask(android.media.AudioFormat.CHANNEL_OUT_MONO)
                .build())
            .setBufferSizeInBytes(length * 2)
            .setTransferMode(android.media.AudioTrack.MODE_STATIC)
            .build();

        railSound.write(samples, 0, length);
      }

      if (railSound.getPlayState() == android.media.AudioTrack.PLAYSTATE_PLAYING) {
        railSound.pause();
      } else {
        railSound.play();
      }
    } catch (Exception error) {
      toast("火車聲未能播放");
      if (railSound != null) {
        railSound.release();
        railSound = null;
      }
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (resultCode != RESULT_OK || data == null || data.getData() == null) {
      return;
    }

    Uri uri = data.getData();

    try {
      getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
    } catch (Exception ignored) {
    }

    if (requestCode == REQUEST_PHOTO) {
      try {
        for (int index = 0; index < tasks.length(); index++) {
          JSONObject task = tasks.optJSONObject(index);
          if (task.optString("id").equals(photoId)) {
            task.put("photo", uri.toString());
          }
        }
        saveTasks();
        render();
      } catch (Exception error) {
        toast("相片未能加入");
      }
    } else if (requestCode == REQUEST_MUSIC) {
      try {
        if (music != null) {
          music.release();
          music = null;
        }

        music = MediaPlayer.create(this, uri);
        if (music == null) {
          throw new IllegalStateException("MediaPlayer could not open the selected audio file");
        }

        music.setLooping(true);
        music.start();
      } catch (Exception error) {
        toast("未能播放呢個檔案");
      }
    }
  }

  @Override
  protected void onPause() {
    super.onPause();

    if (railSound != null) {
      railSound.pause();
    }

    if (music != null && music.isPlaying()) {
      music.pause();
    }
  }

  @Override
  protected void onDestroy() {
    if (railSound != null) {
      railSound.release();
    }

    if (music != null) {
      music.release();
    }

    super.onDestroy();
  }

  private class SceneryView extends View {
    private final android.graphics.Paint paint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);

    SceneryView() {
      super(MainActivity.this);
    }

    private void drawRectangle(android.graphics.Canvas canvas, int color, float left, float top, float right, float bottom) {
      paint.setColor(color);
      canvas.drawRect(left, top, right, bottom, paint);
    }

    @Override
    protected void onDraw(android.graphics.Canvas canvas) {
      float width = getWidth();
      float height = getHeight();
      boolean night = scene.equals("夜行");

      drawRectangle(canvas, Color.rgb(121, 87, 68), 0, 0, width, height);
      drawRectangle(canvas,
          night ? Color.rgb(24, 43, 69)
              : scene.equals("晨光") ? Color.rgb(195, 224, 220)
              : Color.rgb(233, 186, 133),
          dp(8), dp(8), width - dp(8), height - dp(22));

      paint.setColor(Color.rgb(255, 237, 183));
      canvas.drawCircle(width * 0.77f, height * 0.26f, dp(17), paint);

      android.graphics.Path hills = new android.graphics.Path();
      hills.moveTo(dp(8), height * 0.78f);
      hills.lineTo(width * 0.22f, height * 0.35f);
      hills.lineTo(width * 0.42f, height * 0.70f);
      hills.lineTo(width * 0.65f, height * 0.40f);
      hills.lineTo(width - dp(8), height * 0.78f);
      hills.lineTo(width - dp(8), height - dp(22));
      hills.lineTo(dp(8), height - dp(22));
      hills.close();

      paint.setColor(night ? Color.rgb(38, 61, 66) : Color.rgb(100, 131, 107));
      canvas.drawPath(hills, paint);

      drawRectangle(canvas, Color.rgb(66, 93, 73), dp(8), height * 0.80f, width - dp(8), height - dp(22));

      paint.setColor(cream);
      paint.setTextSize(dp(12));
      canvas.drawText(scene + "慢車 / THE OVERLANDER", dp(14), height - dp(6), paint);
    }
  }
}
