package com.nowcoder.community.entity;

import lombok.Data;


/**
 * 封装分页相关的信息
 */
@Data
public class Page {
    //当前页码
    private int currentPage = 1;
    // 显示上限
    private int limit = 10;
    //数据总数（用于计数总页数）
    private int rows;
    //查询路径（用于复用分页链接）
    private String path;
    public void setCurrentPage(int currentPage) {
        if (currentPage<=0){
            return;
        }
        this.currentPage = currentPage;
    }

    public void setLimit(int limit) {
        if (limit<1||limit>100){
            return;
        }
        this.limit = limit;
    }

    public void setRows(int rows) {
        if (rows<0){
            return;
        }
        this.rows = rows;
    }

    /**
     * 获取当前页的起始行
     * @return
     */
    public int getOffset() {
        //current*limit-limit
        return (currentPage - 1) * limit;
    }
    /***
     * description:获取总页数
     * @param []
     * @return int
     */
    public  int getTotal(){
        //rows/limit+1
        return  (rows+limit-1)/limit;
    }
    /***
     * description:获取起始页
     * @param []
     * @return int
     */
    public  int getFrom(){
        int from=currentPage-2;
        return  from<1?1:from;
    }
    /**获取结束页码
     * description:
     * @param []
     * @return int
     */
    public  int getTo(){
        int to=currentPage+2;
        int total = getTotal();
        return  to>total?total:to;
    }

}
