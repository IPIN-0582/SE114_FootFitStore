package com.example.footfitstore.adapter.UserSideAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.footfitstore.R;

import java.util.List;

public class SizeAdapter extends RecyclerView.Adapter<SizeAdapter.SizeViewHolder> {

    private final List<String> sizeList;
    private final Context context;
    private int selectedPosition = -1;  // Biến lưu vị trí kích cỡ được chọn
    private OnSizeSelectedListener sizeSelectedListener;  // Lắng nghe sự kiện chọn kích cỡ

    public SizeAdapter(Context context, List<String> sizeList) {
        this.context = context;
        this.sizeList = sizeList;
    }

    @NonNull
    @Override
    public SizeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_size, parent, false);
        return new SizeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SizeViewHolder holder, int position) {
        String size = sizeList.get(position);
        holder.tvSize.setText(size);

        // Kiểm tra nếu vị trí đang được chọn thì đổi màu nền và màu chữ
        if (selectedPosition == position) {
            holder.itemView.setBackgroundResource(R.drawable.size_seleted_background);  // Nền của thẻ đã chọn
            holder.tvSize.setTextColor(context.getResources().getColor(android.R.color.white));  // Đổi màu chữ thành trắng
        } else {
            holder.itemView.setBackgroundResource(R.drawable.size_background);  // Nền của thẻ chưa chọn
            holder.tvSize.setTextColor(context.getResources().getColor(android.R.color.black));  // Đổi màu chữ thành đen
        }

        // Xử lý sự kiện khi chọn kích cỡ
        holder.itemView.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();  // Lấy vị trí hiện tại của holder

            if (adapterPosition != RecyclerView.NO_POSITION) {  // Đảm bảo rằng vị trí hợp lệ
                selectedPosition = adapterPosition;
                notifyDataSetChanged();  // Cập nhật lại RecyclerView

                if (sizeSelectedListener != null) {
                    sizeSelectedListener.onSizeSelected(sizeList.get(adapterPosition));  // Gửi kích cỡ được chọn ra ngoài
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return sizeList.size();
    }

    // ViewHolder để ánh xạ TextView hiển thị kích cỡ
    public static class SizeViewHolder extends RecyclerView.ViewHolder {
        TextView tvSize;

        public SizeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSize = itemView.findViewById(R.id.tvSize);
        }
    }

    // Interface để lắng nghe khi người dùng chọn kích cỡ
    public interface OnSizeSelectedListener {
        void onSizeSelected(String size);
    }

    public void setOnSizeSelectedListener(OnSizeSelectedListener listener) {
        this.sizeSelectedListener = listener;
    }
}

