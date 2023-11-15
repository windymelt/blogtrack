import { defineConfig } from "vite";
import scalaJSPlugin from "@scala-js/vite-plugin-scalajs";
import globResolverPlugin from "@raquo/vite-plugin-glob-resolver";

export default defineConfig({
  plugins: [scalaJSPlugin({
    cwd: '..',
    projectID: 'widgetJS',
  }),
  globResolverPlugin({
    cwd: __dirname,
    ignore: [
      'node_modules/**',
      'target/**'
    ]
  }),],
});
