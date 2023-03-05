import React, { useState } from "react";
import axios, { AxiosRequestConfig, AxiosResponse, AxiosError } from "axios";
import * as requestType from "./requestBodyTypes";
import * as responseType from "./responseBodyTypes";

const API_URI_ORIGIN = "http://localhost:8888";
const API_URI_PATH = "/override/smp00010";

const App = () => {
    const [products, setProducts] = useState<responseType.buttonGetProductsOnClick[]>([]);
    const [message, setMessage] = useState("");

    const apiUri = API_URI_ORIGIN + API_URI_PATH;

    const requestBody: requestType.buttonGetProductsOnClick = {
        id: 1
    }

    const handleClick = () => {
        setMessage("Clicked!!");

        axios.post(apiUri,
        {
            "id": 1
        },
        {
            headers: {
                "ContentType": "apprication/json",
                "X-HTTP-Method-Override": "GET"
            }
        })
        .then((response: AxiosResponse<responseType.buttonGetProductsOnClick[]>) => {
            setProducts(response.data)
        })
        .catch((e) => console.error(e.response))
    }

    return (
        <div>
            <p><button onClick={handleClick}>メッセージ取得</button></p>
            <table>
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>SeqNo</th>
                        <th>名前</th>
                    </tr>
                </thead>
                <tbody>
                    {
                        products.map(product => (
                            <tr key={ product.id + '-' + product.sequenceNo }>
                                <td>{ product.id }</td>
                                <td>{ product.sequenceNo }</td>
                                <td>{ product.name }</td>
                            </tr>
                        ))
                    }
                </tbody>
            </table>
        </div>
    );
}

export default App;