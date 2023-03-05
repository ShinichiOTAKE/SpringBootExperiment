const path = require("path");

module.exports = {
    mode: "development",

    entry: "./src/main/typescript/index.tsx",

    output: {
        path: path.resolve(__dirname, "public"),
        filename: "main.js"
    },

    module: {
        rules: [
            {
                test: /\.tsx?$/,
                use: "ts-loader"
            }
        ]
    },

    resolve: {
        extensions: [".ts", ".tsx", ".js", "json"]
    },

    target: ["web", "es5"],

    devServer: {
        static: {
            directory: path.resolve(__dirname, "public")
        },
        port: 8080
    }
};