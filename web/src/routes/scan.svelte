<script>
    import {gateway, modal, title} from "$lib/variables";
    import Cropper from "$lib/cropper/Cropper.svelte";
    import {slide} from "svelte/transition";
    import {formSubmit} from "$lib/util";
    import StoreSelector from "$lib/ui/StoreSelector.svelte";
    import {onMount} from "svelte";
    import {goto} from "$app/navigation";

    title.set("Scan");

    let manForm;
    let rows = 2;
    let gate;

    let numRegex = /^[0-9]*?\.?[0-9]{0,2}$/;

    let entryType;

    let items = [];

    $: if (rows != items.length) {
        while (rows > items.length)
            items.push("");
        while (rows < items.length)
            items.pop();
    }

    onMount(() => gate = gateway());

    const testInput = e => {
        if (!numRegex.test(e.target.value + e.key))
            e.preventDefault();
    };

    const submit = () => {
        //TODO Validate the form before submission.
        console.log(manForm);
        formSubmit(manForm, (data) => {
            if (data.error) {
                alert("There was a problem processing these items: " + data.message);
                return;
            }
            console.log(data);

            goto("/items");
        });
    };

    let store;
</script>
<section class="max">
    {#if entryType == "scanner"}
        <div class="scanner" transition:slide>
            <h1>Receipt Scanner</h1>
            <p>*The receipt scanner currently supports receipts from Walmart and Target.</p>
            <p>
                Please try to take an evenly lit picture and crop it down so that just the text is in the text area
                like <span class="blank-button" on:click={() => modal.set({content: 'img:/demoReceipt.jpg'})}>this
            </span>.
            </p>
            <Cropper/>
            <div class="or">OR</div>
            <div class="button" on:click={() => entryType="manual"}>Enter Items Manually</div>
            <a href="/walmart" class="button">Manual Walmart Entry</a>
        </div>
        <!--{:else if !entryType}-->
    {:else if entryType == "manual"}
        <section id="manual" transition:slide>
            <h1>Don't have a receipt? Is your receipt type not supported?</h1>
            <p>Enter your data manually here.</p>
            <form action="{gate}/item-service/receipt/web/items" bind:this={manForm}
                  id="man-form" method="POST">
                <StoreSelector bind:selected={store}/>
                {#if store}
                    <div>Enter items from your {store} receipt.</div>
                {/if}
                {#if rows > 0}
                    <div id="table">
                        {#each Array(rows).fill(1).map((n, i) => i + 1) as r (r)}
                            <div class="row" transition:slide={{duration: 250}}>
                                <fieldset>
                                    {#if items[r]}
                                        <legend>{items[r]}</legend>
                                    {:else}
                                        <legend>Item {r}</legend>
                                    {/if}
                                    <input id="item-{r}-name" name="item-{r}-name" placeholder="Item Name"
                                           type="text" bind:value={items[r]}/>
                                    <input id="item-{r}-upc" name="item-{r}-upc" placeholder="Item UPC" type="text"/>
                                    <input id="item-{r}-price" class="price" name="item-{r}-price"
                                           placeholder="Item Price"
                                           type="number"
                                           min="0.01" step="0.01"
                                           on:keypress={testInput}
                                    />
                                </fieldset>
                            </div>
                        {/each}
                    </div>
                {/if}
            </form>
            <div class="button" on:click={() => rows++}>Add Row
            </div>
            <div class="button" on:click={() => {if(rows > 1) rows--;}}>Remove Row
            </div>
            <div class="button submit" on:click={submit}>Submit</div>
            <div class="or">OR</div>
            <div class="button" on:click={() => entryType="scanner"}>Use a Receipt</div>
            {#if store?.toLowerCase().includes("walmart")}
                <a href="/walmart" class="button">Manual Walmart Entry</a>
            {/if}
        </section>
    {:else}
        <div transition:slide>
            <h3>Please pick an option</h3>
            <div class="button" on:click={() => entryType="scanner"}>Scan Receipt</div>
            <div class="or">OR</div>
            <div class="button" on:click={() => entryType="manual"}>Enter Items Manually</div>
            <a href="/walmart" class="button">Manual Walmart Entry</a>
        </div>
    {/if}
</section>

<style>
    section {
        text-align: center;
    }

    .button.submit {
        background: var(--accent-color);
    }

    input::-webkit-outer-spin-button,
    input::-webkit-inner-spin-button {
        -webkit-appearance: none;
        margin: 0;
    }

    input[type=number] {
        -moz-appearance: textfield;
    }

    #manual {
        overflow-x: auto;
        max-width: 100%;
    }

    span.blank-button {
        color: var(--link-color);
    }

    .row > fieldset {
        position: relative;
        border: unset;
        border-top: 2px solid var(--accent-color);
    }

    .row > fieldset legend {
        padding: 0 0.3em;
        display: block;
        text-align: left;
    }

    .scanner {
        width: 100%;
    }

    @media only screen and (min-width: 768px) {
        #man-form {
            display: flex;
            align-items: center;
            justify-content: center;
            flex-direction: column;
        }

        .row > fieldset {
            display: flex;
            flex-direction: row;
        }
    }

    #table {
        /*border: 1px solid var(--fg-color);*/
        /*display: inline-block;*/
    }

    .row {
        align-items: stretch;
        padding: 0.5rem;
        /*border-bottom: 1px solid black;*/
    }

    .row:last-child {
        border-bottom: none;
    }

    .row > fieldset * {
        display: flex;
        flex-grow: 1;
        padding: 0;
        margin: 0;
    }

    .row > fieldset > * {
        /*align-items: center;*/
        /*flex-direction: column;*/
        padding: 5px 0;
    }

    /*.row:nth-child(even) {*/
    /*    background: #ddd;*/
    /*}*/

    .row input {
        padding: 0.5rem;
        /*border: none;*/
        border: 2px solid #888888;
        border-radius: 0.5em;
        font-size: 0.9rem;
        margin: 0.4rem;
        width: 100%;
        box-sizing: border-box;
        /*border-left: 1px solid rgba(0, 0, 0, 0.5);*/
        background: transparent;
    }

    .row input:focus {
        outline: none;
        border: 2px solid #333;
    }

    #table label {
        margin: 0 10px 0 5px;
    }

    .or {
        font-weight: bold;
        font-style: italic;
        font-size: 0.8em;
        width: 100%;
    }

    .max {
        padding: 0.7rem;
        flex-grow: 1;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
    }

    .demo {
        position: fixed;
        display: flex;
        justify-content: center;
        align-items: center;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        z-index: 100;
    }

    .demo img {
        padding: 2em;
        background: rgba(0, 0, 0, 0.3);
        background: rgba(0, 0, 0, 0.3);
        border-radius: 0.5em;
        max-width: 40%;
        max-height: 80%;
    }

    .hidden {
        display: none;
    }
</style>