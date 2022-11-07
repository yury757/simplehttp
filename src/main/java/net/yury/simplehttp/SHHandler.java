package net.yury.simplehttp;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.HttpMethod;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class SHHandler extends ChannelInboundHandlerAdapter {
    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    private ObjectMapper mapper = new ObjectMapper();
    private SHRouter router;

    public SHHandler(SHRouter router) {
        this.router = router;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        FullHttpRequest request = (FullHttpRequest)msg;
        HttpMethod method = request.method();
        String uri = dealUri(request.uri());
        request.method();
//        HttpHeaders headers = request.headers();
//        String conn = headers.get(HttpHeaderNames.CONNECTION);
//        boolean keepAlive = HttpHeaderValues.KEEP_ALIVE.toString().equals(conn);
        String message;
        String[] res = getContent(method, uri, request.content().toString(DEFAULT_CHARSET));
        uri = res[0];
        String content = res[1];
        Object response = this.router.answer(method, uri, content);
        message = mapper.writeValueAsString(response);
        writeAndFlush(ctx, message, HttpResponseStatus.OK);
    }

    /**
     * 返回结果
     * @author yury
     * @param ctx
     * @param message
     * @param status
     */
    public void writeAndFlush(ChannelHandlerContext ctx, String message, HttpResponseStatus status) {
        DefaultFullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                status,
                Unpooled.copiedBuffer(message, DEFAULT_CHARSET));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json;charset=UTF-8");
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE.toString());
        ctx.writeAndFlush(response);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        cause.printStackTrace();
        String message = cause.getMessage();
        writeAndFlush(ctx, message, HttpResponseStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * 处理 uri
     * @param uri
     * @return
     */
    public String dealUri(String uri) {
        // 剔除 uri 最后面的 /
        int lastIndex = uri.length() - 1;
        int len = uri.length();
        for (int end = len - 1, start = 0; end >= start; end--) {
            if ('/' != uri.charAt(end)) {
                lastIndex = end;
                break;
            }
        }
        if (lastIndex != len - 1) {
            uri = uri.substring(0, lastIndex + 1);
        }
        return uri;
    }

    /**
     * 获取数据内容
     * @param method
     * @param uri
     * @param body
     * @return
     */
    public String[] getContent(HttpMethod method, String uri, String body) {
        String content;
        if (HttpMethod.GET.name().equals(method.name())) {
            String[] split = uri.split("\\?");
            if (split.length > 1) {
                uri = split[0];
                content = split[1];
            } else {
                content = "";
            }
        }else {
            content = body;
        }
        return new String[] {uri, content};
    }

}
