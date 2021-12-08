<script>
    import {gateway, title} from "$lib/variables.js";
    import {formatDate} from "$lib/util.js";
    import {onMount} from "svelte";

    title.set("Walmart Entry");

    let form;

    let storeId, purchaseDate, cardType = "visa", lastFourDigits, total;

    let gate;

    onMount(() => gate = gateway());

    const submit = () => {
        let body = JSON.stringify({
            storeId: ("00000" + (storeId)).slice(-5),
            purchaseDate: formatDate(new Date(purchaseDate)),
            cardType,
            lastFourDigits,
            total
        });

        console.log(body);
        fetch(gate + "/receipt-service/receipt/walmart", {
            method: "post",
            headers: {'Content-Type': 'application/json'},
            body
        })
            .then(res => res.json())
            .then(data => console.log(data))
            .catch(e => console.err);
        // formSubmit(form, (data) => {
        //     console.log(data);
        // });
    };

    let numRegex = /^[0-9]*?\.?[0-9]{0,2}$/;
    const testInput = e => {
        if (!numRegex.test(e.target.value + e.key))
            e.preventDefault();
    };

    const testStoreId = e => {
        if (!/^[0-9]{0,5}$/.test(e.target.value + e.key))
            e.preventDefault();
    };

    const testLastFour = e => {
        if (!/^[0-9]{0,4}$/.test(e.target.value + e.key))
            e.preventDefault();
    };

</script>

<div class="content">
    <h1>Enter your Walmart Receipt Information</h1>
    <p>In order to parse your data, we need a few things from you first...</p>

    <form id="walmartForm" bind:this={form} action="{gate}/receipt-service/receipt/walmart" method="post">
        <label>Store ID</label>
        <input type="text" placeholder="03589" on:keypress={testStoreId} bind:value={storeId}/>
        <label>Purchase Date</label>
        <input type="date" bind:value={purchaseDate}/>
        <label>Card Type</label>
        <select id="card-type" aria-required="true" on:change={e => cardType =
        e.target.children[e.target.selectedIndex].value}>
            <option value="visa">Visa</option>
            <option value="mastercard">Mastercard</option>
            <option value="amex">Amex</option>
            <option value="discover">Discover</option>
            <option value="debit">Debit</option>
            <option value="other">Other</option>
        </select>
        <label>Last Four of Card</label>
        <input type="text" placeholder="####" on:keypress={testLastFour} bind:value={lastFourDigits}/>
        <label>Receipt Total</label>
        <input type="number" min="0" max="10000" step="0.01" placeholder="5.00"
               on:keypress={testInput} bind:value={total}/>
        <span class="submitWrap">
            <div class="button submit" on:click={submit}>Submit</div>
        </span>
    </form>
</div>

<style>

    .content {
        flex-grow: 1;
        padding: 0.7rem;
    }

    .submitWrap {
        text-align: center;
    }

    .submit {
        background: var(--accent-color);
        flex-grow: 0;
    }

    #walmartForm {
        display: flex;
        flex-direction: column;
        max-width: 50%;
        margin: 0 auto;
    }

    #walmartForm > * {
        margin: 0.3rem;
    }

    label {
        display: flex;
        text-decoration: underline;
    }

    label > * {
        margin-left: 0.5rem;
    }

    input, select {
        padding: 0.5rem;
        font-size: 1rem;
        border: 1px solid #888;
        border-radius: 0.3rem;
    }

    @media only screen and (min-width: 768px) {
        .content {
            padding: 1rem;
        }
    }

</style>