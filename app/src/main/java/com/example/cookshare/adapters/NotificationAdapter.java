package com.example.cookshare.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cookshare.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private List<Map<String, Object>> notifications;
    private OnNotificationClickListener onNotificationClickListener;

    public interface OnNotificationClickListener {
        void onNotificationClick(Map<String, Object> notification);
    }

    public NotificationAdapter() {
        this.notifications = new ArrayList<>();
    }

    public void setOnNotificationClickListener(OnNotificationClickListener listener) {
        this.onNotificationClickListener = listener;
    }

    public void updateNotifications(List<Map<String, Object>> newNotifications) {
        this.notifications.clear();
        this.notifications.addAll(newNotifications);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Map<String, Object> notification = notifications.get(position);
        holder.bind(notification);
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    class NotificationViewHolder extends RecyclerView.ViewHolder {
        private TextView notificationTitle;
        private TextView notificationMessage;
        private TextView notificationTime;
        private View notificationIndicator;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            notificationTitle = itemView.findViewById(R.id.notificationTitle);
            notificationMessage = itemView.findViewById(R.id.notificationMessage);
            notificationTime = itemView.findViewById(R.id.notificationTime);
            notificationIndicator = itemView.findViewById(R.id.notificationIndicator);

            itemView.setOnClickListener(v -> {
                if (onNotificationClickListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        onNotificationClickListener.onNotificationClick(notifications.get(position));
                    }
                }
            });
        }

        public void bind(Map<String, Object> notification) {
            // Set title - title đã chứa tên người thích rồi
            String title = (String) notification.get("title");
            notificationTitle.setText(title != null ? title : "Thông báo");

            // Set message - message cũng đã chứa tên người thích
            String message = (String) notification.get("message");
            notificationMessage.setText(message != null ? message : "");

            // Nếu có likedByName riêng, có thể highlight trong message
            String likedByName = (String) notification.get("likedByName");
            if (likedByName != null && !likedByName.isEmpty() && message != null) {
                // Message đã có tên rồi, không cần làm gì thêm
                // Nhưng có thể làm đậm tên trong message nếu muốn (cần SpannableString)
            }

            // Set time
            Object timestampObj = notification.get("timestamp");
            if (timestampObj != null) {
                long timestamp = timestampObj instanceof Long ? (Long) timestampObj
                        : ((Number) timestampObj).longValue();
                notificationTime.setText(formatTime(timestamp));
            } else {
                notificationTime.setText("");
            }

            // Set read indicator
            Object readObj = notification.get("read");
            boolean isRead = readObj != null && (readObj instanceof Boolean ? (Boolean) readObj
                    : readObj.equals(true));
            notificationIndicator.setVisibility(isRead ? View.GONE : View.VISIBLE);

            // Set background color based on read status
            if (isRead) {
                itemView.setAlpha(0.7f);
            } else {
                itemView.setAlpha(1.0f);
            }
        }

        private String formatTime(long timestamp) {
            long now = System.currentTimeMillis();
            long diff = now - timestamp;

            if (diff < 60000) { // Less than 1 minute
                return "Vừa xong";
            } else if (diff < 3600000) { // Less than 1 hour
                long minutes = diff / 60000;
                return minutes + " phút trước";
            } else if (diff < 86400000) { // Less than 1 day
                long hours = diff / 3600000;
                return hours + " giờ trước";
            } else if (diff < 604800000) { // Less than 1 week
                long days = diff / 86400000;
                return days + " ngày trước";
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                return sdf.format(new Date(timestamp));
            }
        }
    }
}
