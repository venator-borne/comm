package com.example.comm.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMeta {
    public String from;
    public String to;
    public String data;
    public Date time;
    public String ext;
}
