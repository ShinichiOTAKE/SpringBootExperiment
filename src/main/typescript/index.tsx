import React from "react";
import {createRoot} from "react-dom/client";

import App from "./feature/smp00010/app";

const container = document.getElementById('root')
if (container) {
    const root = createRoot(container);
    root.render(
        <div>
            <h1>
                Hello world!
            </h1>
            <App/>
        </div>
    )
}