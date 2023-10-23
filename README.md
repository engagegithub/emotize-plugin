# Emotize Plugin for Ant Media Server
This is a plugin project for Ant Media Server.

# Prerequests
- Install Ant Media Server
- Install Maven

# Quick Start

- Clone the repository and go the Emotize Plugin Directory
  ```sh
  git clone git@github.com:engagegithub/emotize-plugin.git
  cd emotize-plugin/
  ```
- Build the Emotize Plugin
  ```sh
  mvn install  -Dgpg.skip=true
  ```
- Copy the generated jar file to your Ant Media Server's plugin directory
  ```sh
  cp target/PluginApp.jar /usr/local/antmedia/plugins
  ```
- Restart the Ant Media Server
  ```
  sudo service antmedia restart
  ```
- Publish/unPublish a Live Stream to Ant Media Server with WebRTC/RTMP/RTSP
- Check the logs on the server side
  ```
  tail -f /usr/local/antmedia/log/ant-media-server.log
  ```
  You would see the following logs
  ```
  ...
  ...
  ...
  io.antmedia.plugin.EmotizePlugin - *************** Stream Started: streamIdXXXXX ***************
  ...
  ...
  ...
  io.antmedia.plugin.EmotizePlugin - *************** Stream Finished: streamIdXXXXX ***************
  ...
  ...
  ...
  ```

For more information about the plugins, [visit this post](https://antmedia.io/plugins-will-make-ant-media-server-more-powerful/)
