import React from 'react';
import { Button, InputGroup, Form } from "react-bootstrap";
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faSearch } from '@fortawesome/free-solid-svg-icons'
import './SearchBar.css';

function SearchBar({ handleSearch, filterValue }) {
    const [input, setInput] = React.useState('');

    React.useEffect(() => {
      if (filterValue !== 1) setInput('');
    }, [filterValue])

    return (
      <InputGroup size="sm">
        <Form.Control value={input} placeholder="Search" aria-label="Search" aria-describedby="search" className="border-light border-2 text-secondary" onChange={(e) => {setInput(e.target.value);}} />
        <Button variant="light" className="border-2" onClick={() => {handleSearch(input)}}><FontAwesomeIcon icon={faSearch} className="text-secondary" /></Button>
      </InputGroup>
    );
  }
  
  export default SearchBar;