<script>
    import {title, variables} from "$lib/variables";
    import Cropper from "$lib/cropper/Cropper.svelte";
    import {slide} from "svelte/transition";
    import {formSubmit} from "$lib/util";

    title.set("Scan");

    let manForm;
    let rows = 2;

    let numRegex = /^[0-9]*?\.?[0-9]{0,2}$/

    let testInput = e => {
        if (!numRegex.test(e.target.value + e.key))
            e.preventDefault();
    };

    const submit = () => {
        formSubmit(manForm, (data) => {
            console.log(data);
        });
    };

</script>
<section>
    <Cropper/>
    <hr>
    <section id="manual">
        <h1>Don't have a receipt? Is your receipt type not supported?</h1>
        <p>Enter your data manually here.</p>
        <form action="{variables.gateway}/item-service/receipt/web/items" bind:this={manForm}
              id="man-form" method="POST">
            <div>
                <label for="store-select">Store: </label>
                <select id="store-select" name="store-select">
                    <option value="WALMART">Walmart</option>
                    <option value="TARGET">Target</option>
                </select>
            </div>
            {#if rows > 0}
                <div id="table">
                    {#each Array(rows).fill(1).map((n, i) => i + 1) as r (r)}
                        <div class="row" transition:slide={{duration: 250}}>
                            <div><label for="item-{r}">Item {r}</label></div>
                            <div><input id="item-{r}" name="item-{r}" placeholder="Item Name" type="text"/></div>
                            <div><input id="item-{r}-upc" name="item-{r}-upc" placeholder="Item UPC" type="text"/></div>
                            <div><input id="item-{r}-price" class="price" name="item-{r}-price" placeholder="Item Price"
                                        type="number"
                                        min="0.01" step="0.01"
                                        on:keypress={testInput}
                            /></div>
                        </div>
                    {/each}
                </div>
            {/if}
        </form>
        <div class="button submit" on:click={submit}>Submit</div>
        <div class="button" on:click={() => rows++}>Add Row
        </div>
        <div class="button" on:click={() => {if(rows > 0) rows--;}}>Remove Row
        </div>
    </section>
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

    #man-form {
        display: flex;
        align-items: center;
        justify-content: center;
        flex-direction: column;
    }

    #table {
        border: 1px solid var(--fg-color);
    }

    .row {
        align-items: stretch;
        border-bottom: 1px solid black;
    }

    .row:last-child {
        border-bottom: none;
    }

    .row, .row * {
        display: flex;
        flex-grow: 1;
        padding: 0;
        margin: 0;
    }

    .row > * {
        align-items: center;
        flex-direction: column;
        padding: 5px 0;
    }

    .row:nth-child(even) {
        background: #ddd;
    }

    .row input {
        padding: 0 5px;
        border: none;
        border-left: 1px solid rgba(0, 0, 0, 0.5);
        background: transparent;
    }

    .row input:focus {
        outline: none;
    }

    #table label {
        margin: 0 10px 0 5px;
    }
</style>