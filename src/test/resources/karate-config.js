function fn() {
  var port = karate.properties['server.port'] || '8080';
  var config = {
    baseUrl: 'http://localhost:' + port
  };

  // Log the base URL for debugging
  karate.log('Base URL:', config.baseUrl);

  // Configure timeouts
  karate.configure('connectTimeout', 5000);
  karate.configure('readTimeout', 5000);

  return config;
}
