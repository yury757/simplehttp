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
        try {
            HttpMethod method = request.method();
            String uri = request.uri();
            request.method();
            HttpHeaders headers = request.headers();
            String conn = headers.get(HttpHeaderNames.CONNECTION);
            boolean keepAlive = HttpHeaderValues.KEEP_ALIVE.toString().equals(conn);
            String content = request.content().toString(DEFAULT_CHARSET);
            Object response = this.router.answer(method, uri, content);
            String message = mapper.writeValueAsString(response);
            ctx.writeAndFlush(build(message, keepAlive));
        }finally {
            request.release();
        }
    }

    public HttpResponse build(String message, boolean keepAlive) {
        DefaultFullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK,
                Unpooled.copiedBuffer(message, DEFAULT_CHARSET));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json;charset=UTF-8");
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        if (keepAlive) response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE.toString());
        return response;
    }
}
