This is simple HTTP server written in Java that expose JVM metrics in Prometheus format

# ENV VARS
`L_PORT`: listen port (default: 9999)

# HTTP Endpoints
`/metrics`: JVM metrics  
`/fujic`: Trigger Full GC  